package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.filesearchwidget.model.MediaFile

@Composable
fun MediaList(
    viewModel: SearchViewModel,
    onFileClick: (MediaFile) -> Unit
) {
    val mediaFiles = viewModel.mediaFiles.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(mediaFiles.itemSnapshotList.items) { _, item ->
            MediaListItem(file = item) {
                onFileClick(item)
            }
        }

        when (val refreshState = mediaFiles.loadState.refresh) {
            is LoadState.Loading -> {
                item { CenteredLoading("Loading...") }
            }
            is LoadState.Error -> {
                item { CenteredError("Error: ${refreshState.error.localizedMessage}") }
            }
            else -> {}
        }

        when (val appendState = mediaFiles.loadState.append) {
            is LoadState.Loading -> {
                item { CenteredLoading("Loading more...") }
            }
            is LoadState.Error -> {
                item { CenteredError("Load more failed: ${appendState.error.localizedMessage}") }
            }
            else -> {}
        }
    }
}
