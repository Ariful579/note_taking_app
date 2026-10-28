package com.notewise.tabnotes.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "note_pages",
    foreignKeys = [
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId"), Index("pageIndex")]
)
data class NotePage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val notebookId: String,
    val pageIndex: Int = 0,
    val template: PageTemplate = PageTemplate.LINED,
    val paperColor: Long = PaperColor.WHITE.hex,
    val strokes: List<Stroke> = emptyList(),
    val textAnnotations: List<TextAnnotation> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
