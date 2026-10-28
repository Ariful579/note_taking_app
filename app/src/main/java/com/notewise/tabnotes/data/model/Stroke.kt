package com.notewise.tabnotes.data.model

import java.util.UUID

data class Point(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Point> = emptyList(),
    val color: Long = 0xFF1E293B, // Default slate dark
    val width: Float = 4.0f,
    val alpha: Float = 1.0f,
    val toolType: ToolType = ToolType.PEN,
    val isClosedShape: Boolean = false
) {
    val bounds: RectF by lazy {
        if (points.isEmpty()) RectF(0f, 0f, 0f, 0f)
        else {
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            for (p in points) {
                if (p.x < minX) minX = p.x
                if (p.x > maxX) maxX = p.x
                if (p.y < minY) minY = p.y
                if (p.y > maxY) maxY = p.y
            }
            RectF(minX, minY, maxX, maxY)
        }
    }

    fun intersects(point: Point, tolerance: Float = 12f): Boolean {
        if (points.size < 2) return false
        val tolSq = tolerance * tolerance
        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]
            val distSq = distanceSqToSegment(point.x, point.y, p1.x, p1.y, p2.x, p2.y)
            if (distSq <= tolSq) return true
        }
        return false
    }

    private fun distanceSqToSegment(x: Float, y: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)
        if (l2 == 0f) return (x - x1) * (x - x1) + (y - y1) * (y - y1)
        var t = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / l2
        t = t.coerceIn(0f, 1f)
        val projX = x1 + t * (x2 - x1)
        val projY = y1 + t * (y2 - y1)
        return (x - projX) * (x - projX) + (y - projY) * (y - projY)
    }
}

data class RectF(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)
