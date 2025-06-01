package com.example.filesearchwidget.model

import android.net.Uri

data class MediaFile(
    val id: Long,
    val uri: Uri,
    val displayName: String?,
    val mimeType: String?,
    val sizeBytes: Long?,              // File size in bytes
    val createdDateMillis: Long?,      // Date file was created
    val modifiedDateMillis: Long?,     // Date file was last modified
    val thumbnailUri: Uri? = null            // Thumbnail URI (for images/videos)
)