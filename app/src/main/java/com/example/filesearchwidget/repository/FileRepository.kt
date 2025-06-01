package com.example.filesearchwidget.repository

import android.content.Context
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.filesearchwidget.model.MediaFile
import com.example.filesearchwidget.paging.MediaStorePagingSource
import com.example.filesearchwidget.paging.SAFDocumentPagingSource
import kotlinx.coroutines.flow.Flow

class FileRepository(private val context: Context) {

    /**
     * Returns a PagingData stream for the requested media type.
     *
     * @param mediaType: "image", "video", "audio", or "document"
     * @param folderUri: required only for "document" mediaType (via SAF)
     * @param searchQuery: user's file name search input
     * @param allowedMimeTypes: optional list of allowed MIME types
     * @param allowedExtensions: optional list of allowed file extensions (especially for documents)
     * @param minFileSizeBytes: optional minimum file size in bytes
     * @param modifiedAfterMillis: optional filter for files modified after this time (epoch millis)
     * @param sortOrder: optional sort order string
     * @param debug: enable debug logs
     */
    fun getMediaFiles(
        mediaType: String,
        folderUri: Uri?,
        searchQuery: String,
        allowedMimeTypes: List<String>? = null,
        allowedExtensions: List<String>? = null,
        minFileSizeBytes: Long? = null,
        modifiedAfterMillis: Long? = null,
        sortOrder: String? = null,
        debug: Boolean = false
    ): Flow<PagingData<MediaFile>> {

        return when (mediaType.lowercase()) {
            "image", "video", "audio" -> {
                Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = {
                        MediaStorePagingSource(
                            context = context,
                            mediaType = mediaType,
                            searchQuery = searchQuery,
                            allowedMimeTypes = allowedMimeTypes,
                            minFileSizeBytes = minFileSizeBytes,
                            modifiedAfterMillis = modifiedAfterMillis,
                            sortOrder = sortOrder,
                            debug = debug
                        )
                    }
                ).flow
            }

            "document" -> {
                if (folderUri == null) {
                    throw IllegalArgumentException("Folder URI must be selected for document search.")
                }

                Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = {
                        SAFDocumentPagingSource(
                            context = context,
                            folderUri = folderUri,
                            searchQuery = searchQuery,
                            allowedMimeTypes = allowedMimeTypes,
                            allowedExtensions = allowedExtensions,
                            minFileSizeBytes = minFileSizeBytes,
                            modifiedAfterMillis = modifiedAfterMillis,
                            sortOrder = sortOrder.toString(),
                            debug = debug
                        )
                    }
                ).flow
            }

            else -> throw IllegalArgumentException("Unsupported media type: $mediaType")
        }
    }
}