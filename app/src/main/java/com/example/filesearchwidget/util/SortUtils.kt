package com.example.filesearchwidget.util

import com.example.filesearchwidget.model.MediaFile
import java.util.Locale

object SortUtils {
    const val DEFAULT_SORT_ORDER = "name ASC"

    fun getSafeSortOrder(order: String?): String {
        return order ?: DEFAULT_SORT_ORDER
    }

    fun sortDocuments(files: List<MediaFile>, sortOrder: String?): List<MediaFile> {
        val (field, ascending) = parseSortOrder(sortOrder)

        val comparator = when (field.lowercase(Locale.ROOT)) {
            "name" -> compareBy<MediaFile> { it.displayName?.lowercase(Locale.ROOT) }
            "size" -> compareBy { it.sizeBytes }
            "date", "modified", "last_modified" -> compareBy { it.modifiedDateMillis }
            else -> compareBy<MediaFile> { it.displayName?.lowercase(Locale.ROOT) } // fallback
        }

        return if (ascending) {
            files.sortedWith(comparator)
        } else {
            files.sortedWith(comparator.reversed())
        }
    }

    private fun parseSortOrder(order: String?): Pair<String, Boolean> {
        val safeOrder = getSafeSortOrder(order).trim().lowercase(Locale.ROOT)

        return when (safeOrder) {
            "name_asc" -> "name" to true
            "name_desc" -> "name" to false
            "size_asc" -> "size" to true
            "size_desc" -> "size" to false
            "oldest_first" -> "modified" to true
            "newest_first" -> "modified" to false
            else -> {
                val parts = safeOrder.split("\\s+".toRegex())
                val field = parts.getOrNull(0) ?: "name"
                val ascending = parts.getOrNull(1)?.uppercase() != "DESC"
                field to ascending
            }
        }
    }
}