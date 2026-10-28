package com.notewise.tabnotes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.notewise.tabnotes.data.model.PageTemplate

@Composable
fun PageTemplateCanvas(
    template: PageTemplate,
    paperColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(paperColor)
    ) {
        val width = size.width
        val height = size.height

        // Determine line colors based on paper brightness
        val isDark = (paperColor.red * 0.299 + paperColor.green * 0.587 + paperColor.blue * 0.114) < 0.5
        val mainLineColor = if (isDark) Color(0x28FFFFFF) else Color(0x24000000)
        val accentLineColor = if (isDark) Color(0x40E11D48) else Color(0x35E11D48) // Margin red
        val dotColor = if (isDark) Color(0x40FFFFFF) else Color(0x36000000)

        when (template) {
            PageTemplate.BLANK -> {
                // Pure clean paper
            }

            PageTemplate.LINED -> {
                val lineSpacing = 36.dp.toPx()
                val leftMargin = 72.dp.toPx()

                // Vertical margin line
                drawLine(
                    color = accentLineColor,
                    start = Offset(leftMargin, 0f),
                    end = Offset(leftMargin, height),
                    strokeWidth = 1.5f
                )

                // Horizontal ruled lines
                var y = 72.dp.toPx()
                while (y < height) {
                    drawLine(
                        color = mainLineColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.0f
                    )
                    y += lineSpacing
                }
            }

            PageTemplate.GRID -> {
                val gridSpacing = 28.dp.toPx()

                // Vertical grid lines
                var x = gridSpacing
                while (x < width) {
                    drawLine(
                        color = mainLineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 0.8f
                    )
                    x += gridSpacing
                }

                // Horizontal grid lines
                var y = gridSpacing
                while (y < height) {
                    drawLine(
                        color = mainLineColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 0.8f
                    )
                    y += gridSpacing
                }
            }

            PageTemplate.DOTTED -> {
                val dotSpacing = 28.dp.toPx()
                val radius = 1.2f

                var y = dotSpacing
                while (y < height) {
                    var x = dotSpacing
                    while (x < width) {
                        drawCircle(
                            color = dotColor,
                            radius = radius,
                            center = Offset(x, y)
                        )
                        x += dotSpacing
                    }
                    y += dotSpacing
                }
            }

            PageTemplate.CORNELL -> {
                val cueColumnX = width * 0.28f
                val summaryY = height - 160.dp.toPx()
                val headerY = 70.dp.toPx()
                val lineSpacing = 32.dp.toPx()

                // Header line
                drawLine(
                    color = accentLineColor,
                    start = Offset(0f, headerY),
                    end = Offset(width, headerY),
                    strokeWidth = 2f
                )

                // Cue vertical divider
                drawLine(
                    color = accentLineColor,
                    start = Offset(cueColumnX, headerY),
                    end = Offset(cueColumnX, summaryY),
                    strokeWidth = 2f
                )

                // Summary horizontal divider
                drawLine(
                    color = accentLineColor,
                    start = Offset(0f, summaryY),
                    end = Offset(width, summaryY),
                    strokeWidth = 2f
                )

                // Notes body lines
                var y = headerY + lineSpacing
                while (y < summaryY) {
                    drawLine(
                        color = mainLineColor,
                        start = Offset(cueColumnX, y),
                        end = Offset(width, y),
                        strokeWidth = 0.9f
                    )
                    y += lineSpacing
                }
            }

            PageTemplate.TASK_LIST -> {
                val rowHeight = 44.dp.toPx()
                val checkX = 40.dp.toPx()
                val boxSize = 18.dp.toPx()

                var y = 60.dp.toPx()
                while (y < height - 60.dp.toPx()) {
                    // Checkbox
                    drawRect(
                        color = mainLineColor,
                        topLeft = Offset(checkX, y - boxSize / 2),
                        size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                    )

                    // Line
                    drawLine(
                        color = mainLineColor,
                        start = Offset(checkX + boxSize + 20.dp.toPx(), y + boxSize / 2),
                        end = Offset(width - 40.dp.toPx(), y + boxSize / 2),
                        strokeWidth = 1.0f
                    )
                    y += rowHeight
                }
            }
        }
    }
}
