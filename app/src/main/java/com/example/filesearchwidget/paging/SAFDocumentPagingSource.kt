package com.example.filesearchwidget.paging

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.filesearchwidget.model.MediaFile
import com.example.filesearchwidget.util.FileFilterUtils
import com.example.filesearchwidget.util.ProjectionUtils
import com.example.filesearchwidget.util.SortUtils

class SAFDocumentPagingSource(
    private val context: Context,
    private val folderUri: Uri,
    private val searchQuery: String,
    private val allowedMimeTypes: List<String>? = null,
    private val allowedExtensions: List<String>? = null,
    private val minFileSizeBytes: Long? = null,
    private val modifiedAfterMillis: Long? = null,
    private val sortOrder: String = SortUtils.DEFAULT_SORT_ORDER,
    private val debug: Boolean = false
) : PagingSource<Int, MediaFile>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaFile> {
        return try {
            if (debug) Log.d(TAG, "Loading page with key=${params.key} and loadSize=${params.loadSize}")

            val allFiles = mutableListOf<MediaFile>()

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                folderUri,
                DocumentsContract.getTreeDocumentId(folderUri)
            )

            context.contentResolver.query(
                childrenUri,
                ProjectionUtils.SAF_PROJECTION,
                null,
                null,
                null  // Ignore SAF sort order, we will sort manually
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val docIdIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val mimeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modifiedIdx = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx)
                    val mimeType = cursor.getString(mimeIdx)
                    val size = cursor.getLong(sizeIdx)
                    val modifiedTime = cursor.getLong(modifiedIdx)

                    if (!FileFilterUtils.matchesSearchQuery(name, searchQuery)) continue
                    if (!FileFilterUtils.matchesMimeType(mimeType, allowedMimeTypes)) continue
                    if (!FileFilterUtils.hasAllowedExtension(name, allowedExtensions)) continue
                    if (!FileFilterUtils.isAboveMinSize(size, minFileSizeBytes)) continue
                    if (!FileFilterUtils.isModifiedAfter(modifiedTime, modifiedAfterMillis)) continue

                    val docId = cursor.getString(docIdIdx)
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)

                    allFiles.add(
                        MediaFile(
                            id = docId.hashCode().toLong(),
                            uri = docUri,
                            displayName = name,
                            mimeType = mimeType,
                            sizeBytes = size,
                            createdDateMillis = modifiedTime,
                            modifiedDateMillis = modifiedTime,
                            thumbnailUri = null
                        )
                    )
                }
            }

            val sortedFiles = SortUtils.sortDocuments(allFiles, sortOrder)

            val pageSize = params.loadSize
            val start = params.key ?: 0
            val end = (start + pageSize).coerceAtMost(sortedFiles.size)
            val pageItems = sortedFiles.subList(start, end)

            val nextKey = if (end >= sortedFiles.size) null else end
            val prevKey = if (start <= 0) null else start - pageSize

            if (debug) {
                Log.d(TAG, "Loaded ${pageItems.size} items. start=$start, end=$end, nextKey=$nextKey, prevKey=$prevKey")
                Log.d(TAG, "Matched files: ${pageItems.joinToString { it.displayName.toString() }}")
            }

            LoadResult.Page(
                data = pageItems,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            if (debug) Log.e(TAG, "Error loading documents", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaFile>): Int? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.plus(state.config.pageSize) ?: page?.nextKey?.minus(state.config.pageSize)
        }
    }

    companion object {
        private const val TAG = "SAFDocumentPagingSource"
    }
}