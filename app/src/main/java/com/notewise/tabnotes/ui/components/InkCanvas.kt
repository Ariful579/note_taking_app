package com.notewise.tabnotes.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke as CanvasStroke
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import com.notewise.tabnotes.data.model.Point
import com.notewise.tabnotes.data.model.Stroke
import com.notewise.tabnotes.data.model.ToolType
import java.util.UUID

@Composable
fun InkCanvas(
    strokes: List<Stroke>,
    selectedTool: ToolType,
    strokeColor: Long,
    strokeWidth: Float,
    highlighterAlpha: Float,
    isStylusOnlyMode: Boolean,
    onAddStroke: (Stroke) -> Unit,
    onEraseAt: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current in-progress stroke points
    var livePoints by remember { mutableStateOf<List<Point>>(emptyList()) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(selectedTool, strokeColor, strokeWidth, isStylusOnlyMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val initialPoint = Point(offset.x, offset.y)
                        if (selectedTool == ToolType.ERASER_STROKE || selectedTool == ToolType.ERASER_POINT) {
                            onEraseAt(initialPoint)
                        } else {
                            livePoints = listOf(initialPoint)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newPoint = Point(
                            x = change.position.x,
                            y = change.position.y,
                            pressure = change.pressure.coerceIn(0.2f, 2.0f)
                        )

                        if (selectedTool == ToolType.ERASER_STROKE || selectedTool == ToolType.ERASER_POINT) {
                            onEraseAt(newPoint)
                        } else {
                            livePoints = livePoints + newPoint
                        }
                    },
                    onDragEnd = {
                        if (livePoints.size >= 2) {
                            val isHighlighter = selectedTool == ToolType.HIGHLIGHTER
                            val isFountain = selectedTool == ToolType.FOUNTAIN_PEN

                            val finalStroke = Stroke(
                                id = UUID.randomUUID().toString(),
                                points = livePoints,
                                color = strokeColor,
                                width = if (isHighlighter) strokeWidth * 2.8f else strokeWidth,
                                alpha = if (isHighlighter) highlighterAlpha else 1.0f,
                                toolType = selectedTool
                            )
                            onAddStroke(finalStroke)
                        }
                        livePoints = emptyList()
                    },
                    onDragCancel = {
                        livePoints = emptyList()
                    }
                )
            }
    ) {
        // 1. Draw all saved completed strokes
        strokes.forEach { stroke ->
            drawSmoothStroke(stroke)
        }

        // 2. Draw live in-progress stroke
        if (livePoints.size >= 2) {
            val isHighlighter = selectedTool == ToolType.HIGHLIGHTER
            val liveStroke = Stroke(
                points = livePoints,
                color = strokeColor,
                width = if (isHighlighter) strokeWidth * 2.8f else strokeWidth,
                alpha = if (isHighlighter) highlighterAlpha else 1.0f,
                toolType = selectedTool
            )
            drawSmoothStroke(liveStroke)
        }
    }
}

/**
 * Renders a stroke using quadratic Bézier curve interpolation between consecutive midpoints.
 * This delivers authentic, professional-grade handwriting smoothness without polygonal angular artifacts.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothStroke(stroke: Stroke) {
    val points = stroke.points
    if (points.isEmpty()) return

    if (points.size == 1) {
        // Draw a single dot for a tap
        val p = points.first()
        drawCircle(
            color = Color(stroke.color).copy(alpha = stroke.alpha),
            radius = stroke.width / 2f,
            center = Offset(p.x, p.y)
        )
        return
    }

    val path = Path()
    path.moveTo(points[0].x, points[0].y)

    if (points.size == 2) {
        path.lineTo(points[1].x, points[1].y)
    } else {
        // Quadratic Bézier curve through midpoints
        for (i in 1 until points.size - 1) {
            val current = points[i]
            val next = points[i + 1]
            val midX = (current.x + next.x) / 2f
            val midY = (current.y + next.y) / 2f
            path.quadraticTo(current.x, current.y, midX, midY)
        }
        // Connect last segment
        val last = points.last()
        path.lineTo(last.x, last.y)
    }

    val isHighlighter = stroke.toolType == ToolType.HIGHLIGHTER
    drawPath(
        path = path,
        color = Color(stroke.color).copy(alpha = stroke.alpha),
        style = CanvasStroke(
            width = stroke.width,
            cap = if (isHighlighter) StrokeCap.Square else StrokeCap.Round,
            join = if (isHighlighter) StrokeJoin.Bevel else StrokeJoin.Round
        )
    )
}
