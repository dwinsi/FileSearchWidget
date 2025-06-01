package com.example.filesearchwidget.paging

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.filesearchwidget.model.MediaFile
import com.example.filesearchwidget.util.MediaConstants
import com.example.filesearchwidget.util.MediaCursorMapper
import com.example.filesearchwidget.util.SortUtils
import com.example.filesearchwidget.util.MediaStoreQueryBuilder
import java.util.Locale

class MediaStorePagingSource(
    private val context: Context,
    mediaType: String,
    private val searchQuery: String,
    private val allowedMimeTypes: List<String>? = null,
    private val minFileSizeBytes: Long? = null,
    private val modifiedAfterMillis: Long? = null,
    private val sortOrder: String? = SortUtils.DEFAULT_SORT_ORDER,
    private val debug: Boolean = false
) : PagingSource<Int, MediaFile>() {

    private val normalizedMediaType = mediaType.lowercase(Locale.getDefault())

    init {
        val validTypes = setOf(MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO)
        require(normalizedMediaType in validTypes) {
            "Invalid mediaType: $mediaType. Must be one of $validTypes"
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MediaFile>): Int? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.plus(state.config.pageSize) ?: page?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaFile> {
        return try {
            if (debug) Log.d(TAG, "Loading MediaStore page with key=${params.key} and loadSize=${params.loadSize}")
            if (debug) Log.d(TAG, "Media type used for query: $normalizedMediaType")

            val uri: Uri = when (normalizedMediaType) {
                MediaType.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external") // Should never reach here because of validation
            }

            val selectionParts = mutableListOf<String>()
            val selectionArgs = mutableListOf<String>()

            if (searchQuery.isNotEmpty()) {
                selectionParts.add("${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?")
                selectionArgs.add("%$searchQuery%")
            }

            allowedMimeTypes?.takeIf { it.isNotEmpty() }?.let {
                selectionParts.add("${MediaStore.MediaColumns.MIME_TYPE} IN (${it.joinToString(",") { "?" }})")
                selectionArgs.addAll(it)
            }

            minFileSizeBytes?.takeIf { it > 0 }?.let {
                selectionParts.add("${MediaStore.MediaColumns.SIZE} >= ?")
                selectionArgs.add(it.toString())
            }

            modifiedAfterMillis?.takeIf { it > 0 }?.let {
                selectionParts.add("${MediaStore.MediaColumns.DATE_MODIFIED} >= ?")
                selectionArgs.add((it / 1000).toString())
            }

            val selection = if (selectionParts.isNotEmpty()) selectionParts.joinToString(" AND ") else null

            val queryArgs = MediaStoreQueryBuilder.buildQueryArgs(
                page = (params.key ?: 0),
                pageSize = params.loadSize,
                sortOrder = sortOrder,
                selection = selection,
                selectionArgs = selectionArgs.toTypedArray()
            )

            val allFiles = context.contentResolver.query(
                uri,
                MediaConstants.projection,
                queryArgs,
                null
            )?.use { cursor ->
                MediaCursorMapper.mapCursorToMediaFiles(context, cursor, uri)
            } ?: emptyList()

            val sortedFiles = SortUtils.sortDocuments(allFiles, sortOrder)

            val pageItems = sortedFiles

            val currentPage = params.key ?: 0
            val nextKey = if (pageItems.size < params.loadSize) null else currentPage + 1
            val prevKey = if (currentPage == 0) null else currentPage - 1

            if (debug) {
                val currentPage = params.key ?: 0
                Log.d(TAG, "Loaded ${pageItems.size} items on page $currentPage, nextKey=$nextKey")
                Log.d(TAG, "Matched files: ${pageItems.joinToString { it.displayName ?: "Unnamed" }}")
            }

            LoadResult.Page(
                data = pageItems,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            if (debug) Log.e(TAG, "Error loading from MediaStore", e)
            LoadResult.Error(e)
        }
    }

    companion object {
        private const val TAG = "MediaStorePagingSource"
    }
}

object MediaType {
    const val IMAGE = "image"
    const val VIDEO = "video"
    const val AUDIO = "audio"
}