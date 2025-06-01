package com.example.filesearchwidget.ui.components

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.filesearchwidget.model.MediaFile
import com.example.filesearchwidget.repository.FileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

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

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    private val debug = true
    private val allowedExtensions = listOf("pdf", "doc", "docx", "txt", "epub")

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

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
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
        combine(_mediaType, _folderUri, _searchQuery, _sortOrder) { type, folder, search, sort ->
            Quadruple(type, folder, search, sort)
        }
            .debounce(300)
            .flatMapLatest { (type, folder, search, sort) ->
                if (type == "document" && folder == null) {
                    flowOf(PagingData.empty())
                } else {
                    repo.getMediaFiles(
                        mediaType = type,
                        folderUri = folder,
                        searchQuery = search,
                        allowedExtensions = allowedExtensions,
                        sortOrder = sort.sortString, // ✅ Uses enum's built-in string
                        debug = debug
                    )
                }
            }
            .cachedIn(viewModelScope)
}

// ✅ Enum with direct sort string mapping
enum class SortOrder(val label: String, val sortString: String) {
    NAME_ASC("Name (A-Z)", "name"),
    NEWEST_FIRST("Newest First", "modified DESC"),
    OLDEST_FIRST("Oldest First", "modified ASC");

    override fun toString(): String = label
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)