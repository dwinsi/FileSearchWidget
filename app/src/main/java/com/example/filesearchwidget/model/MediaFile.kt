package com.example.filesearchwidget.model

import android.net.Uri

data class MediaFile(
    val id: Long,
    val uri: Uri,
    val displayName: String?,
    val mimeType: String?,
    val sizeBytes: Long?,        // add this
    val createdDateMillis: Long? // add this
)