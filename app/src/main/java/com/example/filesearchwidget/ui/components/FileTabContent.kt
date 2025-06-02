package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.filesearchwidget.model.MediaFile
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.unit.IntOffset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTabContent(
    viewModel: SearchViewModel,
    onFileClick: (MediaFile) -> Unit,
    isGridView: Boolean
) {
    // 1. Collect media files from ViewModel
    val mediaFiles = viewModel.mediaFiles.collectAsLazyPagingItems()

    // 2. UI states for list and grid
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val coroutineScope = rememberCoroutineScope()

    // 3. States for filtering, sorting and selection
    var filterText by remember { mutableStateOf("") }
    var sortByName by remember { mutableStateOf(true) }
    val selectedFiles = remember { mutableStateListOf<MediaFile>() }

    // Helper function for toggling selection
    fun toggleSelection(file: MediaFile) {
        if (selectedFiles.contains(file)) {
            selectedFiles.remove(file)
        } else {
            selectedFiles.add(file)
        }
    }

    // 4. Filter & Sort logic (simple, on currently loaded items)
    val filteredSortedFiles = remember(mediaFiles.itemCount, filterText, sortByName) {
        // Collect currently loaded items into a list (filter & sort)
        val loadedItems = (0 until mediaFiles.itemCount)
            .mapNotNull { mediaFiles[it] }
            .filter {
                it.displayName?.contains(filterText, ignoreCase = true) ?: false
            }
            .sortedWith(
                if (sortByName) {
                    compareBy { it.displayName?.lowercase() ?: "" }
                } else {
                    compareByDescending { it.createdDateMillis ?: 0L } // or dateCreated
                }
            )
        loadedItems
    }

    Column(Modifier.fillMaxSize()) {
        // --- Filtering input ---
        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            label = { Text("Filter by name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // --- Sorting toggle ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sort by: ", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { sortByName = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sortByName) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            ) {
                Text("Name")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { sortByName = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!sortByName) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            ) {
                Text("Date Added")
            }
        }

        // --- Selected count ---
        if (selectedFiles.isNotEmpty()) {
            Text(
                "${selectedFiles.size} selected",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // --- File list or grid ---
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val containerHeight = constraints.maxHeight.toFloat()
            val totalItems = filteredSortedFiles.size

            if (isGridView) {
                val columns = 3
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(totalItems) { index ->
                        val file = filteredSortedFiles[index]
                        // Wrap with selection highlight
                        val isSelected = selectedFiles.contains(file)

                        Box(
                            modifier = Modifier
                                .then(
                                    if (isSelected) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.medium
                                    ) else Modifier
                                )
                                .clickable {
                                    toggleSelection(file)
                                    onFileClick(file)
                                }
                        ) {
                            FileItemGrid(file = file, onClick = { /* onClick handled above */ })
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(totalItems) { index ->
                        val file = filteredSortedFiles[index]
                        val isSelected = selectedFiles.contains(file)
                        Box(
                            modifier = Modifier
                                .then(
                                    if (isSelected) Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    ) else Modifier
                                )
                                .clickable {
                                    toggleSelection(file)
                                    onFileClick(file)
                                }
                        ) {
                            FileItem(file = file, onClick = { /* onClick handled above */ })
                        }
                    }
                }

                // Custom Scrollbar for list view remains the same as before
                if (totalItems > 0) {
                    val visibleItems = listState.layoutInfo.visibleItemsInfo.size
                    if (visibleItems > 0) {
                        val thumbHeightPx =
                            (visibleItems.toFloat() / totalItems * containerHeight).coerceAtLeast(48f)
                        val scrollProgress =
                            listState.firstVisibleItemIndex.toFloat() / (totalItems - visibleItems)
                                .coerceAtLeast(1)
                        val thumbOffsetPx = scrollProgress * (containerHeight - thumbHeightPx)

                        var dragOffset by remember { mutableStateOf(0f) }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(32.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(0, (thumbOffsetPx + dragOffset).roundToInt()) }
                                    .width(12.dp)
                                    .height(thumbHeightPx.dp)
                                    .align(Alignment.TopEnd)
                                    .background(
                                        Color.DarkGray.copy(alpha = 0.6f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .pointerInput(totalItems, visibleItems) {
                                        detectVerticalDragGestures(
                                            onDragStart = { dragOffset = 0f },
                                            onVerticalDrag = { change, dragAmount ->
                                                dragOffset += dragAmount
                                                val newOffsetPx =
                                                    (thumbOffsetPx + dragOffset).coerceIn(
                                                        0f,
                                                        containerHeight - thumbHeightPx
                                                    )
                                                val newScrollIndex =
                                                    ((newOffsetPx / (containerHeight - thumbHeightPx)) * (totalItems - visibleItems))
                                                        .roundToInt()
                                                        .coerceIn(0, totalItems - visibleItems)

                                                coroutineScope.launch {
                                                    listState.scrollToItem(newScrollIndex)
                                                }
                                                change.consume()
                                            },
                                            onDragEnd = { dragOffset = 0f },
                                            onDragCancel = { dragOffset = 0f }
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}