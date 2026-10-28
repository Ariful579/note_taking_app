package com.notewise.tabnotes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Notebook",
    val folderId: String? = null,
    val coverColor: Long = 0xFF3B82F6, // Default cobalt / Notewise blue
    val isFavorite: Boolean = false,
    val pageCount: Int = 1,
    val defaultTemplate: PageTemplate = PageTemplate.LINED,
    val defaultPaperColor: Long = PaperColor.WHITE.hex,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconName: String = "folder",
    val color: Long = 0xFF64748B,
    val createdAt: Long = System.currentTimeMillis()
)
