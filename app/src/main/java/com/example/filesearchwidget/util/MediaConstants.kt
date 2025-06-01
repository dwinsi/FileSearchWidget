package com.example.filesearchwidget.util

object MediaConstants {
    val projection = arrayOf(
        android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
        android.provider.MediaStore.MediaColumns._ID,
        android.provider.MediaStore.MediaColumns.MIME_TYPE,
        android.provider.MediaStore.MediaColumns.SIZE,
        android.provider.MediaStore.MediaColumns.DATE_ADDED,
        android.provider.MediaStore.MediaColumns.DATE_MODIFIED
    )
}