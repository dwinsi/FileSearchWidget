package com.example.filesearchwidget.util

import android.content.ContentResolver
import android.os.Bundle
import android.provider.MediaStore

object MediaStoreQueryBuilder {

    fun buildQueryArgs(
        page: Int,
        pageSize: Int,
        sortOrder: String?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Bundle {
        val (sortColumn, sortDirection) = when (sortOrder?.lowercase()) {
            "name_asc" -> MediaStore.MediaColumns.DISPLAY_NAME to ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
            "name_desc" -> MediaStore.MediaColumns.DISPLAY_NAME to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            "oldest_first" -> MediaStore.MediaColumns.DATE_ADDED to ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
            "newest_first", null, "" -> MediaStore.MediaColumns.DATE_ADDED to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            else -> MediaStore.MediaColumns.DATE_ADDED to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
        }

        return Bundle().apply {
            putInt(ContentResolver.QUERY_ARG_LIMIT, pageSize)
            putInt(ContentResolver.QUERY_ARG_OFFSET, page * pageSize)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(sortColumn))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, sortDirection)
            selection?.let { putString(ContentResolver.QUERY_ARG_SQL_SELECTION, it) }
            selectionArgs?.let { putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, it) }
        }
    }
}