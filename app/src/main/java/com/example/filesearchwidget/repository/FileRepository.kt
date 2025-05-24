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
     * @param allowedExtensions: Optional list of allowed file extensions (for documents)
     * @param debug: Enable debug logging
     */
    fun getMediaFiles(
        mediaType: String,
        folderUri: Uri?,
        searchQuery: String,
        allowedExtensions: List<String>? = null,
        debug: Boolean = false
    ): Flow<PagingData<MediaFile>> {

        return when (mediaType.lowercase()) {
            "image", "video", "audio" -> {
                Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = {
                        MediaStorePagingSource(context, mediaType, searchQuery)
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
                            allowedExtensions = allowedExtensions, // Pass as is, null or empty means no filtering
                            minFileSizeBytes = 0, // no min file size, get all files
                            modifiedAfterMillis = 0L, // no date filtering
                            debug = debug
                        )
                    }
                ).flow
            }

            else -> throw IllegalArgumentException("Unsupported media type: $mediaType")
        }
    }
}