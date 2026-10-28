package com.notewise.tabnotes.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notewise.tabnotes.data.model.PageTemplate
import com.notewise.tabnotes.data.model.PaperColor
import com.notewise.tabnotes.data.model.ToolType
import com.notewise.tabnotes.ui.theme.*

@Composable
fun StylusToolbar(
    selectedTool: ToolType,
    strokeColor: Long,
    strokeWidth: Float,
    selectedTemplate: PageTemplate,
    selectedPaperColor: Long,
    canUndo: Boolean,
    canRedo: Boolean,
    isStylusOnlyMode: Boolean,
    onSelectTool: (ToolType) -> Unit,
    onSelectColor: (Long) -> Unit,
    onSelectWidth: (Float) -> Unit,
    onSelectTemplate: (PageTemplate) -> Unit,
    onSelectPaperColor: (Long) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearPage: () -> Unit,
    onToggleStylusMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showWidthSlider by remember { mutableStateOf(false) }
    var showTemplateMenu by remember { mutableStateOf(false) }

    val presetColors = listOf(
        0xFF0F172A, // Black
        0xFF1D4ED8, // Blue
        0xFFDC2626, // Red
        0xFF16A34A, // Green
        0xFF9333EA, // Purple
        0xFFFFE066, // Soft Yellow
        0xFFF472B6, // Soft Pink
        0xFF86EFAC  // Soft Mint
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Toolset: Pen, Fountain, Highlighter, Eraser, Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ToolButton(
                        icon = Icons.Default.Edit,
                        label = "Pen",
                        isSelected = selectedTool == ToolType.PEN,
                        onClick = { onSelectTool(ToolType.PEN) }
                    )

                    ToolButton(
                        icon = Icons.Default.Brush,
                        label = "Fountain",
                        isSelected = selectedTool == ToolType.FOUNTAIN_PEN,
                        onClick = { onSelectTool(ToolType.FOUNTAIN_PEN) }
                    )

                    ToolButton(
                        icon = Icons.Default.BorderColor,
                        label = "Highlighter",
                        isSelected = selectedTool == ToolType.HIGHLIGHTER,
                        onClick = { onSelectTool(ToolType.HIGHLIGHTER) }
                    )

                    ToolButton(
                        icon = Icons.Default.AutoFixNormal,
                        label = "Eraser",
                        isSelected = selectedTool == ToolType.ERASER_STROKE,
                        onClick = { onSelectTool(ToolType.ERASER_STROKE) }
                    )

                    ToolButton(
                        icon = Icons.Default.TextFields,
                        label = "Text",
                        isSelected = selectedTool == ToolType.TEXT,
                        onClick = { onSelectTool(ToolType.TEXT) }
                    )
                }

                // Center Palette: Quick Color Chips & Stroke Width Trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetColors.take(5).forEach { colorHex ->
                        val isSelected = strokeColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33000000),
                                    shape = CircleShape
                                )
                                .clickable { onSelectColor(colorHex) }
                        )
                    }

                    // Stroke Width Button
                    FilterChip(
                        selected = showWidthSlider,
                        onClick = { showWidthSlider = !showWidthSlider },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(strokeWidth.coerceIn(3f, 16f).dp)
                                        .clip(CircleShape)
                                        .background(Color(strokeColor))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${strokeWidth.toInt()}px", fontSize = 12.sp)
                            }
                        }
                    )
                }

                // Right Controls: Paper Template, Stylus Lock, Undo, Redo, Clear
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Paper Template & Color Selector
                    Box {
                        IconButton(onClick = { showTemplateMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Page Template",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showTemplateMenu,
                            onDismissRequest = { showTemplateMenu = false }
                        ) {
                            Text(
                                "Page Template",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            PageTemplate.entries.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.displayName) },
                                    onClick = {
                                        onSelectTemplate(template)
                                        showTemplateMenu = false
                                    },
                                    leadingIcon = {
                                        if (selectedTemplate == template) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                            HorizontalDivider()
                            Text(
                                "Paper Color",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            PaperColor.entries.forEach { color ->
                                DropdownMenuItem(
                                    text = { Text(color.displayName) },
                                    onClick = {
                                        onSelectPaperColor(color.hex)
                                        showTemplateMenu = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(color.hex))
                                                .border(1.dp, Color.Gray, CircleShape)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Stylus Palm Rejection Mode Toggle
                    IconButton(onClick = onToggleStylusMode) {
                        Icon(
                            imageVector = if (isStylusOnlyMode) Icons.Default.PanTool else Icons.Default.TouchApp,
                            contentDescription = "Stylus Only Mode",
                            tint = if (isStylusOnlyMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Undo
                    IconButton(onClick = onUndo, enabled = canUndo) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Redo
                    IconButton(onClick = onRedo, enabled = canRedo) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Clear Page
                    IconButton(onClick = onClearPage) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Page",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Expandable Width Slider Bar
            AnimatedVisibility(visible = showWidthSlider) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thickness:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = strokeWidth,
                            onValueChange = onSelectWidth,
                            valueRange = 1f..32f,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${strokeWidth.toInt()} dp", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(38.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp)
        )
    }
}
