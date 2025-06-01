package com.example.filesearchwidget.util

object FileFilterUtils {
    fun matchesSearchQuery(name: String, query: String): Boolean {
        return query.isEmpty() || name.contains(query, ignoreCase = true)
    }

    fun matchesMimeType(mimeType: String, allowedMimeTypes: List<String>?): Boolean {
        return allowedMimeTypes.isNullOrEmpty() || allowedMimeTypes.any { mimeType == it || mimeType.startsWith(it) }
    }

    fun hasAllowedExtension(name: String, allowedExtensions: List<String>?): Boolean {
        if (allowedExtensions.isNullOrEmpty()) return true
        val lowerName = name.lowercase()
        return allowedExtensions.any { ext -> lowerName.endsWith(".$ext") }
    }

    fun isAboveMinSize(size: Long, minSize: Long?): Boolean {
        return minSize == null || size >= minSize
    }

    fun isModifiedAfter(modifiedTime: Long, modifiedAfterMillis: Long?): Boolean {
        return modifiedAfterMillis == null || modifiedTime >= modifiedAfterMillis
    }
}