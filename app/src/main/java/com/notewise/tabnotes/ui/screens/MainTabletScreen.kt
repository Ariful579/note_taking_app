package com.notewise.tabnotes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notewise.tabnotes.ui.components.*
import com.notewise.tabnotes.viewmodel.EditorUiState
import com.notewise.tabnotes.viewmodel.LibraryViewModel
import com.notewise.tabnotes.viewmodel.NoteEditorViewModel

@Composable
fun MainTabletScreen(
    editorViewModel: NoteEditorViewModel,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    var currentNavDestination by remember { mutableStateOf(TabletNavDestination.EDITOR) }
    val editorState by editorViewModel.uiState.collectAsState()
    val strokes by editorViewModel.currentStrokes.collectAsState()

    Row(modifier = modifier.fillMaxSize()) {
        // 1. Tablet Left Navigation Rail
        TabletNavigationRail(
            currentDestination = currentNavDestination,
            onNavigate = { destination ->
                if (destination == TabletNavDestination.FAVORITES) {
                    libraryViewModel.setFavoritesFilter(true)
                    currentNavDestination = TabletNavDestination.LIBRARY
                } else if (destination == TabletNavDestination.LIBRARY) {
                    libraryViewModel.setFavoritesFilter(false)
                    currentNavDestination = TabletNavDestination.LIBRARY
                } else {
                    currentNavDestination = TabletNavDestination.EDITOR
                }
            },
            onNewNotebook = {
                editorViewModel.createNewNotebookAndOpenTab()
                currentNavDestination = TabletNavDestination.EDITOR
            }
        )

        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // 2. Main Content Canvas or Library Gallery
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (currentNavDestination) {
                TabletNavDestination.LIBRARY, TabletNavDestination.FAVORITES -> {
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onOpenNotebook = { notebook ->
                            editorViewModel.openNotebookInTab(notebook)
                            currentNavDestination = TabletNavDestination.EDITOR
                        }
                    )
                }

                TabletNavDestination.EDITOR -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Multi-Document Tab Bar
                        NoteTabBar(
                            openTabs = editorState.openTabs,
                            activeTabId = editorState.activeTabId,
                            onSelectTab = { editorViewModel.selectTab(it) },
                            onCloseTab = { editorViewModel.closeTab(it) },
                            onNewTabClick = { editorViewModel.createNewNotebookAndOpenTab() }
                        )

                        if (editorState.activeTabId != null) {
                            // Stylus & Drawing Toolbar
                            StylusToolbar(
                                selectedTool = editorState.selectedTool,
                                strokeColor = editorState.strokeColor,
                                strokeWidth = editorState.strokeWidth,
                                selectedTemplate = editorState.pageTemplate,
                                selectedPaperColor = editorState.paperColor,
                                canUndo = editorState.canUndo,
                                canRedo = editorState.canRedo,
                                isStylusOnlyMode = editorState.isStylusOnlyMode,
                                onSelectTool = { editorViewModel.selectTool(it) },
                                onSelectColor = { editorViewModel.setStrokeColor(it) },
                                onSelectWidth = { editorViewModel.setStrokeWidth(it) },
                                onSelectTemplate = { editorViewModel.changePageTemplate(it) },
                                onSelectPaperColor = { editorViewModel.changePaperColor(it) },
                                onUndo = { editorViewModel.undo() },
                                onRedo = { editorViewModel.redo() },
                                onClearPage = { editorViewModel.clearCurrentPage() },
                                onToggleStylusMode = { editorViewModel.toggleStylusOnlyMode() }
                            )

                            // Note Canvas Workspace
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                // Background Template (Lined, Grid, Cornell, etc.)
                                PageTemplateCanvas(
                                    template = editorState.pageTemplate,
                                    paperColor = Color(editorState.paperColor)
                                )

                                // Foreground Low-Latency Ink Canvas
                                InkCanvas(
                                    strokes = strokes,
                                    selectedTool = editorState.selectedTool,
                                    strokeColor = editorState.strokeColor,
                                    strokeWidth = editorState.strokeWidth,
                                    highlighterAlpha = editorState.highlighterAlpha,
                                    isStylusOnlyMode = editorState.isStylusOnlyMode,
                                    onAddStroke = { editorViewModel.addStroke(it) },
                                    onEraseAt = { editorViewModel.eraseStrokesAt(it) }
                                )

                                // Floating Tablet Page Navigator (Bottom Center)
                                PageNavigator(
                                    currentPage = editorState.pageIndex,
                                    totalPages = editorState.pageCount,
                                    onPreviousPage = { editorViewModel.goToPage(editorState.pageIndex - 1) },
                                    onNextPage = { editorViewModel.goToPage(editorState.pageIndex + 1) },
                                    onAddPage = { editorViewModel.addNewPage() },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 24.dp)
                                )
                            }
                        } else {
                            // Empty State when all tabs are closed
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No open notebook tabs",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { editorViewModel.createNewNotebookAndOpenTab() }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open New Tab")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
