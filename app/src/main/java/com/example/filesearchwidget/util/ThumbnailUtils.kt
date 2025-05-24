package com.example.filesearchwidget.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadThumbnailCompat(context: Context, uri: Uri): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.loadThumbnail(uri, Size(96, 96), null)
        } catch (e: Exception) {
            null
        }
    }