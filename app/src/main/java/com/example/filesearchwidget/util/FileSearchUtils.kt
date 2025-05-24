package com.example.filesearchwidget.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.example.filesearchwidget.model.MediaFile

fun searchMediaFiles(context: Context, mediaType: String, query: String): List<MediaFile> {
    val collection = when (mediaType) {
        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        else -> return emptyList()
    }

    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE
    )

    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
    val selectionArgs = arrayOf("%$query%")
    val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

    val files = mutableListOf<MediaFile>()

    context.contentResolver.query(
        collection,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val mimeType = cursor.getString(mimeTypeColumn)
            val uri = Uri.withAppendedPath(collection, id.toString())

            val documentUri = DocumentsContract.buildDocumentUriUsingTree(uri, id.toString())
        }
    }

    return files
}

fun searchDocuments(context: Context, rootUri: Uri, query: String): List<MediaFile> {
    val files = mutableListOf<MediaFile>()

    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
        rootUri,
        DocumentsContract.getTreeDocumentId(rootUri)
    )

    context.contentResolver.query(
        childrenUri,
        arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        ),
        "${DocumentsContract.Document.COLUMN_DISPLAY_NAME} LIKE ?",
        arrayOf("%$query%"),
        null
    )?.use { cursor ->
        val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        val typeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

        while (cursor.moveToNext()) {
            val documentId = cursor.getString(idIndex)
            val name = cursor.getString(nameIndex)
            val mimeType = cursor.getString(typeIndex)

            val docUri = DocumentsContract.buildDocumentUriUsingTree(rootUri, documentId)
        }
    }

    return files
}