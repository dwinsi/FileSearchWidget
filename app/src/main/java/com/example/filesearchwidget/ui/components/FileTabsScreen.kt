package com.example.filesearchwidget.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.filesearchwidget.model.MediaFile


@Composable
fun FileTabsScreen(
    viewModel: SearchViewModel,
    onPickFolder: () -> Unit,
    onFileClick: (MediaFile) -> Unit
) {
    val tabTitles = listOf("Photos", "Videos", "Audio", "Documents")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        // Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        // Change media type when tab is selected
        LaunchedEffect(selectedTabIndex) {
            val type = when (selectedTabIndex) {
                0 -> "image"
                1 -> "video"
                2 -> "audio"
                3 -> "document"
                else -> "image"
            }
            viewModel.setMediaType(type)
        }

        // Sort dropdown UI
        val currentSortOrder by viewModel.sortOrder.collectAsState()
        SortOrderDropdown(
            selectedOrder = currentSortOrder,
            onOrderSelected = { viewModel.setSortOrder(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Tab-specific content
        when (selectedTabIndex) {
            3 -> DocumentTab(viewModel, onPickFolder, onFileClick)
            else -> FileTabContent(viewModel, onFileClick)
        }
    }
}

@Composable
fun DocumentTab(
    viewModel: SearchViewModel,
    onPickFolder: () -> Unit,
    onFileClick: (MediaFile) -> Unit
) {
    val folderUri by viewModel.folderUri.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastShownUri by remember { mutableStateOf<Uri?>(null) }

    // Snackbar for folder selection
    LaunchedEffect(folderUri) {
        if (folderUri != lastShownUri) {
            lastShownUri = folderUri
            if (folderUri != null) {
                snackbarHostState.showSnackbar("Folder selected successfully")
            } else {
                snackbarHostState.showSnackbar("Folder cleared")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Folder path and buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = folderUri?.lastPathSegment ?: "No folder selected",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onPickFolder) {
                Text("Change Folder")
            }
            if (folderUri != null) {
                TextButton(onClick = { viewModel.clearFolder() }) {
                    Text("Clear")
                }
            }
        }

        // Search box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            placeholder = { Text("Search documents...") },
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        )

        // Shared file listing
        FileTabContent(viewModel, onFileClick)
    }
}