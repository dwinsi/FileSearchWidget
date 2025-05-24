package com.example.filesearchwidget

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.*
import com.example.filesearchwidget.ui.components.FileTabsScreen
import com.example.filesearchwidget.ui.components.SearchViewModel
import com.example.filesearchwidget.ui.theme.FileSearchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SearchViewModel by viewModels()

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                viewModel.setSelectedFolder(it)
            }
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (!granted) {
            Toast.makeText(this, "Permissions are required to access media files.", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        permissionsToRequest.addAll(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        )

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FileSearchTheme {
                Surface {
                    FileTabsScreen(
                        viewModel = viewModel,
                        onPickFolder = { folderPickerLauncher.launch(null) },
                        onFileClick = { mediaFile ->
                            viewModel.openFile(this@MainActivity, mediaFile)
                        }
                    )
                }
            }
        }
        requestPermissions()
    }
}