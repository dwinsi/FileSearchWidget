package com.example.filesearchwidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.filesearchwidget.ui.components.FileTabsScreen
import com.example.filesearchwidget.ui.theme.FileSearchTheme
import com.example.filesearchwidget.ui.components.SearchViewModel

class SearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: SearchViewModel by viewModels()
            val context = LocalContext.current
            val needFolderSelection by viewModel.needFolderSelection.collectAsState()

            val pickFolderLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree()
            ) { uri: Uri? ->
                if (uri != null) {
                    try {
                        // Persist permission (read + write for future access)
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        viewModel.setSelectedFolder(uri)
                    } catch (e: SecurityException) {
                        Toast.makeText(context, "Failed to persist folder access permission.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Folder selection cancelled. Cannot proceed with document search.", Toast.LENGTH_SHORT).show()
                }
            }

            LaunchedEffect(needFolderSelection) {
                if (needFolderSelection) {
                    pickFolderLauncher.launch(null)
                }
            }

            FileSearchTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FileTabsScreen(
                        viewModel = viewModel,
                        onFileClick = { file -> viewModel.openFile(context, file) },
                        onPickFolder = { pickFolderLauncher.launch(null) }
                    )
                }
            }
        }
    }
}