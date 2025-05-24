package com.example.filesearchwidget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FileSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onPickFolder: () -> Unit,
    showFolderPicker: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search files...") },
            modifier = Modifier.weight(1f)
        )

        if (showFolderPicker) {
            Button(onClick = onPickFolder) {
                Text("Pick Folder")
            }
        }
    }
}