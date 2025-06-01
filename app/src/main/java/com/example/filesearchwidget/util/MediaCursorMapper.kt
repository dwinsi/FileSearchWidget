package com.example.filesearchwidget.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.example.filesearchwidget.model.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaCursorMapper {

    suspend fun mapCursorToMediaFiles(context: Context, cursor: Cursor, uri: Uri): List<MediaFile> = withContext(Dispatchers.IO) {
        val nameIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DISPLAY_NAME)
        val idIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns._ID)
        val mimeTypeIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.MIME_TYPE)
        val sizeIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.SIZE)
        val dateAddedIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATE_ADDED)
        val dateModifiedIndex = cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns.DATE_MODIFIED)

        val mediaList = mutableListOf<MediaFile>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idIndex)
            val name = cursor.getString(nameIndex)
            val mimeType = cursor.getString(mimeTypeIndex)
            val sizeBytes = cursor.getLong(sizeIndex)
            val createdDateMillis = cursor.getLong(dateAddedIndex) * 1000L
            val modifiedDateMillis = cursor.getLong(dateModifiedIndex) * 1000L
            val fileUri = ContentUris.withAppendedId(uri, id)

            // Generate thumbnail URI only for images and videos
            val thumbnailUri = if (mimeType?.startsWith("image") == true || mimeType?.startsWith("video") == true) {
                ThumbnailUtils.generateThumbnailUri(context, fileUri)
            } else {
                null
            }

            mediaList.add(
                MediaFile(
                    id = id,
                    uri = fileUri,
                    displayName = name,
                    mimeType = mimeType,
                    sizeBytes = sizeBytes,
                    createdDateMillis = createdDateMillis,
                    modifiedDateMillis = modifiedDateMillis,
                    thumbnailUri = thumbnailUri
                )
            )
        }

        mediaList
    }
}