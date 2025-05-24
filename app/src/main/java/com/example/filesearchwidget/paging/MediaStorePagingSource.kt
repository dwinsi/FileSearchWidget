package com.example.filesearchwidget.paging

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.filesearchwidget.model.MediaFile
import android.net.Uri

class MediaStorePagingSource(
    private val context: Context,
    private val mediaType: String,
    private val searchQuery: String
) : PagingSource<Int, MediaFile>() {

    override fun getRefreshKey(state: PagingState<Int, MediaFile>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaFile> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        val uri = when (mediaType) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED
        )

        val selection = if (searchQuery.isNotEmpty()) {
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        } else null

        val selectionArgs = if (searchQuery.isNotEmpty()) {
            arrayOf("%$searchQuery%")
        } else null

        val queryArgs = Bundle().apply {
            putInt(ContentResolver.QUERY_ARG_LIMIT, pageSize)
            putInt(ContentResolver.QUERY_ARG_OFFSET, page * pageSize)
            putString(ContentResolver.QUERY_ARG_SORT_COLUMNS, MediaStore.MediaColumns.DATE_ADDED)
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            if (selection != null && selectionArgs != null) {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            }
        }

        return try {
            val mediaList = mutableListOf<MediaFile>()

            context.contentResolver.query(uri, projection, queryArgs, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex)
                    val mimeType = cursor.getString(mimeTypeIndex)
                    val sizeBytes = cursor.getLong(sizeIndex)
                    val dateAddedSeconds = cursor.getLong(dateAddedIndex)
                    val createdDateMillis = dateAddedSeconds * 1000L

                    val fileUri = ContentUris.withAppendedId(uri, id)

                    mediaList.add(
                        MediaFile(
                            id = id,
                            uri = fileUri,
                            displayName = name,
                            mimeType = mimeType,
                            sizeBytes = sizeBytes,
                            createdDateMillis = createdDateMillis
                        )
                    )
                }
            }

            LoadResult.Page(
                data = mediaList,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (mediaList.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}