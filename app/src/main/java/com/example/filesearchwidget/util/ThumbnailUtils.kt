package com.example.filesearchwidget.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ThumbnailUtils {

    private const val THUMBNAIL_SIZE = 96
    private const val TAG = "ThumbnailUtils"

    /**
     * Generates a thumbnail for a given content Uri and stores it in the app's cache directory.
     * Returns a file Uri pointing to the generated thumbnail image.
     */
    suspend fun generateThumbnailUri(context: Context, uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap: Bitmap = context.contentResolver.loadThumbnail(uri, Size(THUMBNAIL_SIZE, THUMBNAIL_SIZE), null)

            val thumbFile = File(context.cacheDir, "thumb_${uri.hashCode()}.jpg")
            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            bitmap.recycle() // free memory
            Uri.fromFile(thumbFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail for $uri", e)
            null
        }
    }
}