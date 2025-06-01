package com.example.filesearchwidget.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

object ThumbnailUtils {

    fun generateThumbnailUri(context: Context, fileUri: Uri, mimeType: String?): Uri? {
        return try {
            when {
                mimeType?.startsWith("image") == true ||
                        mimeType?.startsWith("video") == true -> {
                    // Use loadThumbnail for images/videos (API 29+)
                    val bitmap: Bitmap = context.contentResolver.loadThumbnail(fileUri, android.util.Size(96, 96), null)
                    saveBitmapToCacheAndGetUri(context, bitmap)
                }

                mimeType?.startsWith("audio") == true -> {
                    // No thumbnail for audio, return placeholder
                    getAudioPlaceholderUri(context)
                }

                else -> getFilePlaceholderUri(context)
            }
        } catch (e: Exception) {
            Log.w("ThumbnailUtils", "Thumbnail generation failed: ${e.message}")
            getFilePlaceholderUri(context)
        }
    }

    private fun getAudioPlaceholderUri(context: Context): Uri =
        "android.resource://${context.packageName}/drawable/ic_audio_placeholder".toUri()

    private fun getFilePlaceholderUri(context: Context): Uri =
        "android.resource://${context.packageName}/drawable/ic_file_placeholder".toUri()

    private fun saveBitmapToCacheAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val cacheDir = File(context.cacheDir, "thumbnails")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val file = File(cacheDir, "${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return file.toUri()
    }
}