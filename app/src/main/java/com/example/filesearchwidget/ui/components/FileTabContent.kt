package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.filesearchwidget.model.MediaFile
import com.example.filesearchwidget.ui.components.SearchViewModel

@Composable
fun FileTabContent(
    viewModel: SearchViewModel,
    onFileClick: (MediaFile) -> Unit
) {
    val mediaFiles = viewModel.mediaFiles.collectAsLazyPagingItems()

    LazyColumn {
        items(mediaFiles.itemCount) { index ->
            val file = mediaFiles[index]
            file?.let {
                FileItem(file = it, onClick = { onFileClick(it) })
            }
        }
    }
}