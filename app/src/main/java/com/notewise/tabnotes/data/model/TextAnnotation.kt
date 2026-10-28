package com.notewise.tabnotes.data.model

import java.util.UUID

data class TextAnnotation(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val fontSizeSp: Float = 16f,
    val color: Long = 0xFF1E293B,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val width: Float = 300f
)
