package com.notewise.tabnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notewise.tabnotes.data.model.Folder
import com.notewise.tabnotes.data.model.Notebook
import com.notewise.tabnotes.data.model.PageTemplate
import com.notewise.tabnotes.data.model.PaperColor
import com.notewise.tabnotes.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val notebooks: List<Notebook> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedFolderId: String? = null,
    val showOnlyFavorites: Boolean = false,
    val searchQuery: String = "",
    val isCreateNotebookDialogOpen: Boolean = false,
    val isCreateFolderDialogOpen: Boolean = false
)

class LibraryViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allNotebooks,
                repository.allFolders,
                _uiState.map { it.selectedFolderId }.distinctUntilChanged(),
                _uiState.map { it.showOnlyFavorites }.distinctUntilChanged(),
                _uiState.map { it.searchQuery }.distinctUntilChanged()
            ) { allNotebooks, allFolders, folderId, favoritesOnly, query ->
                var filtered = allNotebooks

                if (folderId != null) {
                    filtered = filtered.filter { it.folderId == folderId }
                }

                if (favoritesOnly) {
                    filtered = filtered.filter { it.isFavorite }
                }

                if (query.isNotBlank()) {
                    filtered = filtered.filter { it.title.contains(query, ignoreCase = true) }
                }

                _uiState.value.copy(
                    notebooks = filtered,
                    folders = allFolders
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }

    fun selectFolder(folderId: String?) {
        _uiState.update { it.copy(selectedFolderId = folderId, showOnlyFavorites = false) }
    }

    fun setFavoritesFilter(onlyFavorites: Boolean) {
        _uiState.update { it.copy(showOnlyFavorites = onlyFavorites, selectedFolderId = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openCreateNotebookDialog() {
        _uiState.update { it.copy(isCreateNotebookDialogOpen = true) }
    }

    fun closeCreateNotebookDialog() {
        _uiState.update { it.copy(isCreateNotebookDialogOpen = false) }
    }

    fun openCreateFolderDialog() {
        _uiState.update { it.copy(isCreateFolderDialogOpen = true) }
    }

    fun closeCreateFolderDialog() {
        _uiState.update { it.copy(isCreateFolderDialogOpen = false) }
    }

    fun createNotebook(
        title: String,
        coverColor: Long,
        template: PageTemplate,
        paperColor: Long = PaperColor.WHITE.hex,
        onCreated: (Notebook) -> Unit = {}
    ) {
        viewModelScope.launch {
            val nb = repository.createNotebook(
                title = title.ifBlank { "Untitled Notebook" },
                folderId = _uiState.value.selectedFolderId,
                coverColor = coverColor,
                template = template,
                paperColor = paperColor
            )
            closeCreateNotebookDialog()
            onCreated(nb)
        }
    }

    fun createFolder(name: String, color: Long = 0xFF64748B) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createFolder(name = name, color = color)
            closeCreateFolderDialog()
        }
    }

    fun toggleFavorite(notebook: Notebook) {
        viewModelScope.launch {
            repository.updateNotebook(notebook.copy(isFavorite = !notebook.isFavorite))
        }
    }

    fun deleteNotebook(notebook: Notebook) {
        viewModelScope.launch {
            repository.deleteNotebook(notebook)
        }
    }
}
