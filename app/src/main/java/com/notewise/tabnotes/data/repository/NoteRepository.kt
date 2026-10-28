package com.notewise.tabnotes.data.repository

import com.notewise.tabnotes.data.local.NoteDao
import com.notewise.tabnotes.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NoteRepository(private val noteDao: NoteDao) {

    val allNotebooks: Flow<List<Notebook>> = noteDao.getAllNotebooks()
    val allFolders: Flow<List<Folder>> = noteDao.getAllFolders()
    val favoriteNotebooks: Flow<List<Notebook>> = noteDao.getFavoriteNotebooks()

    fun getNotebooksInFolder(folderId: String): Flow<List<Notebook>> =
        noteDao.getNotebooksInFolder(folderId)

    fun getNotebookById(id: String): Flow<Notebook?> =
        noteDao.getNotebookById(id)

    fun getPagesForNotebook(notebookId: String): Flow<List<NotePage>> =
        noteDao.getPagesForNotebook(notebookId)

    fun getPage(notebookId: String, pageIndex: Int): Flow<NotePage?> =
        noteDao.getPage(notebookId, pageIndex)

    suspend fun getNotebookByIdOnce(id: String): Notebook? =
        noteDao.getNotebookByIdOnce(id)

    suspend fun getPageOnce(notebookId: String, pageIndex: Int): NotePage? =
        noteDao.getPageOnce(notebookId, pageIndex)

    suspend fun createNotebook(
        title: String = "Untitled Notebook",
        folderId: String? = null,
        coverColor: Long = 0xFF3B82F6,
        template: PageTemplate = PageTemplate.LINED,
        paperColor: Long = PaperColor.WHITE.hex
    ): Notebook {
        val notebook = Notebook(
            id = UUID.randomUUID().toString(),
            title = title,
            folderId = folderId,
            coverColor = coverColor,
            pageCount = 1,
            defaultTemplate = template,
            defaultPaperColor = paperColor,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        noteDao.insertNotebook(notebook)

        // Initialize first page
        val firstPage = NotePage(
            id = UUID.randomUUID().toString(),
            notebookId = notebook.id,
            pageIndex = 0,
            template = template,
            paperColor = paperColor
        )
        noteDao.insertPage(firstPage)

        return notebook
    }

    suspend fun updateNotebook(notebook: Notebook) {
        noteDao.updateNotebook(notebook.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNotebook(notebook: Notebook) {
        noteDao.deletePagesForNotebook(notebook.id)
        noteDao.deleteNotebook(notebook)
    }

    suspend fun savePageStrokesAndAnnotations(
        notebookId: String,
        pageIndex: Int,
        strokes: List<Stroke>,
        annotations: List<TextAnnotation>
    ) {
        val existing = noteDao.getPageOnce(notebookId, pageIndex)
        if (existing != null) {
            val updated = existing.copy(
                strokes = strokes,
                textAnnotations = annotations,
                updatedAt = System.currentTimeMillis()
            )
            noteDao.updatePage(updated)
        } else {
            val notebook = noteDao.getNotebookByIdOnce(notebookId)
            val newPage = NotePage(
                notebookId = notebookId,
                pageIndex = pageIndex,
                template = notebook?.defaultTemplate ?: PageTemplate.LINED,
                paperColor = notebook?.defaultPaperColor ?: PaperColor.WHITE.hex,
                strokes = strokes,
                textAnnotations = annotations
            )
            noteDao.insertPage(newPage)
        }

        // Touch notebook updated timestamp
        val nb = noteDao.getNotebookByIdOnce(notebookId)
        if (nb != null) {
            noteDao.updateNotebook(nb.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun addNewPage(notebookId: String, template: PageTemplate? = null, paperColor: Long? = null): NotePage {
        val count = noteDao.getPageCount(notebookId)
        val notebook = noteDao.getNotebookByIdOnce(notebookId)
        val newPage = NotePage(
            notebookId = notebookId,
            pageIndex = count,
            template = template ?: notebook?.defaultTemplate ?: PageTemplate.LINED,
            paperColor = paperColor ?: notebook?.defaultPaperColor ?: PaperColor.WHITE.hex
        )
        noteDao.insertPage(newPage)

        if (notebook != null) {
            noteDao.updateNotebook(notebook.copy(pageCount = count + 1, updatedAt = System.currentTimeMillis()))
        }
        return newPage
    }

    suspend fun createFolder(name: String, color: Long = 0xFF64748B): Folder {
        val folder = Folder(name = name, color = color)
        noteDao.insertFolder(folder)
        return folder
    }

    suspend fun deleteFolder(folder: Folder) {
        noteDao.deleteFolder(folder)
    }
}
