package com.example.filesearchwidget.paging

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.filesearchwidget.model.MediaFile

class SAFDocumentPagingSource(
    private val context: Context,
    private val folderUri: Uri,
    private val searchQuery: String,
    private val allowedMimeTypes: List<String>? = null,
    private val allowedExtensions: List<String>? = null,
    private val minFileSizeBytes: Long? = null,
    private val modifiedAfterMillis: Long? = null,
    private val debug: Boolean = false
) : PagingSource<Int, MediaFile>() {

    private fun hasAllowedExtension(name: String): Boolean {
        if (allowedExtensions.isNullOrEmpty()) return true
        val lowerName = name.lowercase()
        return allowedExtensions.any { ext -> lowerName.endsWith(".$ext") }
    }

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
                arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                ),
                null,
                null,
                "${DocumentsContract.Document.COLUMN_DISPLAY_NAME} ASC"
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

                    if (searchQuery.isNotEmpty() && !name.contains(searchQuery, ignoreCase = true)) continue
                    if (!allowedMimeTypes.isNullOrEmpty() && allowedMimeTypes.none { mimeType.startsWith(it) || mimeType == it }) continue
                    if (!hasAllowedExtension(name)) continue
                    if (minFileSizeBytes != null && size < minFileSizeBytes) continue
                    if (modifiedAfterMillis != null && modifiedTime < modifiedAfterMillis) continue

                    val docId = cursor.getString(docIdIdx)
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, docId)

                    // Add size and createdDateMillis (modifiedTime here)
                    allFiles.add(
                        MediaFile(
                            id = docId.hashCode().toLong(),  // Generate a long ID from String
                            uri = docUri,
                            displayName = name,
                            mimeType = mimeType,
                            sizeBytes = size,
                            createdDateMillis = modifiedTime
                        )
                    )
                }
            }

            val pageSize = params.loadSize
            val start = params.key ?: 0
            val end = (start + pageSize).coerceAtMost(allFiles.size)
            val pageItems = allFiles.subList(start, end)

            val nextKey = if (end < allFiles.size) end else null
            val prevKey = if (start == 0) null else (start - pageSize).coerceAtLeast(0)

            if (debug) {
                Log.d(TAG, "Loaded ${pageItems.size} items. start=$start, end=$end, nextKey=$nextKey, prevKey=$prevKey")
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