package com.example.filesearchwidget.ui.components

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.filesearchwidget.model.MediaFile
import com.example.filesearchwidget.repository.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import androidx.core.content.edit
import androidx.core.net.toUri

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FileRepository(app)

    private val _folderUri = MutableStateFlow<Uri?>(null)
    val folderUri: StateFlow<Uri?> = _folderUri.asStateFlow()

    private val _mediaType = MutableStateFlow("image") // Default type
    val mediaType: StateFlow<String> = _mediaType.asStateFlow()

    private val _needFolderSelection = MutableStateFlow(false)
    val needFolderSelection: StateFlow<Boolean> = _needFolderSelection.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Optional: enable debug logging
    private val debug = true

    // Optional: define allowed file extensions for documents
    private val allowedExtensions = listOf("pdf", "doc", "docx", "txt")

    init {
        loadPersistedFolderUri()
    }

    private fun loadPersistedFolderUri() {
        val uriString = getApplication<Application>()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getString("folder_uri", null)

        uriString?.let {
            _folderUri.value = it.toUri()
        }
    }

    fun setSelectedFolder(uri: Uri) {
        _folderUri.value = uri

        getApplication<Application>()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit {
                putString("folder_uri", uri.toString())
            }

        _needFolderSelection.value = false
    }

    fun requestFolderIfNeeded() {
        if (_mediaType.value.equals("document", ignoreCase = true) && _folderUri.value == null) {
            _needFolderSelection.value = true
        } else {
            _needFolderSelection.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMediaType(type: String) {
        _mediaType.value = type
        requestFolderIfNeeded()
    }

    fun clearFolder() {
        _folderUri.value = null
        getApplication<Application>()
            .getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit {
                remove("folder_uri")
            }
        _needFolderSelection.value = true
    }

    fun openFile(context: Context, mediaFile: MediaFile) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(mediaFile.uri, mediaFile.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open file", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val mediaFiles: Flow<PagingData<MediaFile>> =
        combine(_mediaType, _folderUri, _searchQuery) { type, folder, search ->
            Triple(type, folder, search)
        }
            .debounce(300)
            .flatMapLatest { (type, folder, search) ->
                if (type == "document" && folder == null) {
                    // Return empty PagingData if no folder selected for documents
                    flowOf(PagingData.empty())
                } else {
                    repo.getMediaFiles(
                        mediaType = type,
                        folderUri = folder,
                        searchQuery = search,
                        allowedExtensions = allowedExtensions,
                        debug = debug
                    )
                }
            }
            .cachedIn(viewModelScope)
}