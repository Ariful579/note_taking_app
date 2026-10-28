package com.notewise.tabnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notewise.tabnotes.data.model.*
import com.notewise.tabnotes.data.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NotebookTab(
    val notebookId: String,
    val title: String,
    val coverColor: Long,
    val currentPageIndex: Int = 0
)

data class EditorUiState(
    val openTabs: List<NotebookTab> = emptyList(),
    val activeTabId: String? = null,
    val activeNotebook: Notebook? = null,
    val activePage: NotePage? = null,
    val pageIndex: Int = 0,
    val pageCount: Int = 1,
    val selectedTool: ToolType = ToolType.PEN,
    val strokeColor: Long = 0xFF0F172A, // InkBlack
    val strokeWidth: Float = 4.0f,
    val highlighterAlpha: Float = 0.40f,
    val paperColor: Long = PaperColor.WHITE.hex,
    val pageTemplate: PageTemplate = PageTemplate.LINED,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isStylusOnlyMode: Boolean = false,
    val scale: Float = 1.0f,
    val panOffset: Pair<Float, Float> = Pair(0f, 0f)
)

class NoteEditorViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _currentStrokes = MutableStateFlow<List<Stroke>>(emptyList())
    val currentStrokes: StateFlow<List<Stroke>> = _currentStrokes.asStateFlow()

    private val _textAnnotations = MutableStateFlow<List<TextAnnotation>>(emptyList())
    val textAnnotations: StateFlow<List<TextAnnotation>> = _textAnnotations.asStateFlow()

    private val undoStack = mutableListOf<List<Stroke>>()
    private val redoStack = mutableListOf<List<Stroke>>()

    private var autoSaveJob: Job? = null

    init {
        // Observe all notebooks so if title or page count changes, tabs stay synced
        viewModelScope.launch {
            repository.allNotebooks.collect { allList ->
                val currentTabs = _uiState.value.openTabs
                if (currentTabs.isEmpty() && allList.isNotEmpty()) {
                    // Open the most recently updated notebook in the first tab by default
                    openNotebookInTab(allList.first())
                } else {
                    // Sync tab titles/covers
                    val updatedTabs = currentTabs.map { tab ->
                        val matched = allList.find { it.id == tab.notebookId }
                        if (matched != null) {
                            tab.copy(title = matched.title, coverColor = matched.coverColor)
                        } else tab
                    }
                    _uiState.update { it.copy(openTabs = updatedTabs) }
                }
            }
        }
    }

    // ==========================================
    // Multi-Tab Management
    // ==========================================

    fun openNotebookInTab(notebook: Notebook) {
        val currentTabs = _uiState.value.openTabs.toMutableList()
        val existingIndex = currentTabs.indexOfFirst { it.notebookId == notebook.id }

        if (existingIndex == -1) {
            val newTab = NotebookTab(
                notebookId = notebook.id,
                title = notebook.title,
                coverColor = notebook.coverColor,
                currentPageIndex = 0
            )
            currentTabs.add(newTab)
        }

        _uiState.update {
            it.copy(
                openTabs = currentTabs,
                activeTabId = notebook.id,
                activeNotebook = notebook,
                pageIndex = 0,
                pageCount = notebook.pageCount
            )
        }
        loadPage(notebook.id, 0)
    }

    fun selectTab(notebookId: String) {
        if (_uiState.value.activeTabId == notebookId) return
        saveCurrentPageImmediately()

        val tab = _uiState.value.openTabs.find { it.notebookId == notebookId } ?: return
        viewModelScope.launch {
            val notebook = repository.getNotebookByIdOnce(notebookId)
            _uiState.update {
                it.copy(
                    activeTabId = notebookId,
                    activeNotebook = notebook,
                    pageIndex = tab.currentPageIndex,
                    pageCount = notebook?.pageCount ?: 1
                )
            }
            loadPage(notebookId, tab.currentPageIndex)
        }
    }

    fun closeTab(notebookId: String) {
        saveCurrentPageImmediately()
        val currentTabs = _uiState.value.openTabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.notebookId == notebookId }
        if (index == -1) return

        currentTabs.removeAt(index)

        if (currentTabs.isEmpty()) {
            _uiState.update {
                it.copy(
                    openTabs = emptyList(),
                    activeTabId = null,
                    activeNotebook = null,
                    activePage = null
                )
            }
            _currentStrokes.value = emptyList()
            _textAnnotations.value = emptyList()
            undoStack.clear()
            redoStack.clear()
        } else {
            val newActiveIndex = if (index >= currentTabs.size) currentTabs.size - 1 else index
            val nextTab = currentTabs[newActiveIndex]
            _uiState.update { it.copy(openTabs = currentTabs) }
            selectTab(nextTab.notebookId)
        }
    }

    fun createNewNotebookAndOpenTab(title: String = "Quick Note", template: PageTemplate = PageTemplate.LINED) {
        viewModelScope.launch {
            val notebook = repository.createNotebook(title = title, template = template)
            openNotebookInTab(notebook)
        }
    }

    // ==========================================
    // Page Management
    // ==========================================

    private fun loadPage(notebookId: String, pageIndex: Int) {
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoAvailability()

        viewModelScope.launch {
            val page = repository.getPageOnce(notebookId, pageIndex)
            if (page != null) {
                _currentStrokes.value = page.strokes
                _textAnnotations.value = page.textAnnotations
                _uiState.update {
                    it.copy(
                        activePage = page,
                        pageIndex = pageIndex,
                        pageTemplate = page.template,
                        paperColor = page.paperColor
                    )
                }
            } else {
                _currentStrokes.value = emptyList()
                _textAnnotations.value = emptyList()
            }
        }
    }

    fun goToPage(targetIndex: Int) {
        val nbId = _uiState.value.activeTabId ?: return
        if (targetIndex < 0 || targetIndex >= _uiState.value.pageCount) return
        if (targetIndex == _uiState.value.pageIndex) return

        saveCurrentPageImmediately()

        // Update tab's current page index
        val updatedTabs = _uiState.value.openTabs.map {
            if (it.notebookId == nbId) it.copy(currentPageIndex = targetIndex) else it
        }
        _uiState.update { it.copy(openTabs = updatedTabs, pageIndex = targetIndex) }
        loadPage(nbId, targetIndex)
    }

    fun addNewPage() {
        val nbId = _uiState.value.activeTabId ?: return
        saveCurrentPageImmediately()

        viewModelScope.launch {
            val newPage = repository.addNewPage(
                notebookId = nbId,
                template = _uiState.value.pageTemplate,
                paperColor = _uiState.value.paperColor
            )
            val updatedNb = repository.getNotebookByIdOnce(nbId)
            _uiState.update {
                it.copy(
                    activeNotebook = updatedNb,
                    pageCount = updatedNb?.pageCount ?: (it.pageCount + 1)
                )
            }
            goToPage(newPage.pageIndex)
        }
    }

    fun changePageTemplate(template: PageTemplate) {
        _uiState.update { it.copy(pageTemplate = template) }
        scheduleAutoSave()
    }

    fun changePaperColor(colorHex: Long) {
        _uiState.update { it.copy(paperColor = colorHex) }
        scheduleAutoSave()
    }

    // ==========================================
    // Tool & Drawing Operations
    // ==========================================

    fun selectTool(tool: ToolType) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun setStrokeColor(color: Long) {
        _uiState.update { it.copy(strokeColor = color) }
    }

    fun setStrokeWidth(width: Float) {
        _uiState.update { it.copy(strokeWidth = width.coerceIn(1.0f, 60.0f)) }
    }

    fun toggleStylusOnlyMode() {
        _uiState.update { it.copy(isStylusOnlyMode = !it.isStylusOnlyMode) }
    }

    fun addStroke(stroke: Stroke) {
        // Push state for undo
        undoStack.add(_currentStrokes.value.toList())
        redoStack.clear()

        _currentStrokes.value = _currentStrokes.value + stroke
        updateUndoRedoAvailability()
        scheduleAutoSave()
    }

    fun eraseStrokesAt(point: Point, radius: Float = 16f) {
        val strokes = _currentStrokes.value
        val surviving = strokes.filterNot { it.intersects(point, radius) }
        if (surviving.size != strokes.size) {
            undoStack.add(strokes.toList())
            redoStack.clear()
            _currentStrokes.value = surviving
            updateUndoRedoAvailability()
            scheduleAutoSave()
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previousState = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_currentStrokes.value.toList())
            _currentStrokes.value = previousState
            updateUndoRedoAvailability()
            scheduleAutoSave()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val nextState = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_currentStrokes.value.toList())
            _currentStrokes.value = nextState
            updateUndoRedoAvailability()
            scheduleAutoSave()
        }
    }

    fun clearCurrentPage() {
        if (_currentStrokes.value.isNotEmpty() || _textAnnotations.value.isNotEmpty()) {
            undoStack.add(_currentStrokes.value.toList())
            redoStack.clear()
            _currentStrokes.value = emptyList()
            _textAnnotations.value = emptyList()
            updateUndoRedoAvailability()
            scheduleAutoSave()
        }
    }

    fun addTextAnnotation(annotation: TextAnnotation) {
        _textAnnotations.value = _textAnnotations.value + annotation
        scheduleAutoSave()
    }

    private fun updateUndoRedoAvailability() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    // ==========================================
    // Auto-Save
    // ==========================================

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            saveCurrentPageImmediately()
        }
    }

    fun saveCurrentPageImmediately() {
        val nbId = _uiState.value.activeTabId ?: return
        val pIndex = _uiState.value.pageIndex
        val strokesToSave = _currentStrokes.value
        val annotationsToSave = _textAnnotations.value

        viewModelScope.launch {
            repository.savePageStrokesAndAnnotations(
                notebookId = nbId,
                pageIndex = pIndex,
                strokes = strokesToSave,
                annotations = annotationsToSave
            )
        }
    }
}
