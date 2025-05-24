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
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

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

        FileTabContent(viewModel, onFileClick)
    }
}