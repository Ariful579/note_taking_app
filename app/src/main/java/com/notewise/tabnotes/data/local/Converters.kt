package com.notewise.tabnotes.data.local

import androidx.room.TypeConverter
import com.notewise.tabnotes.data.model.*
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromPageTemplate(value: PageTemplate): String = value.name

    @TypeConverter
    fun toPageTemplate(value: String): PageTemplate = try {
        PageTemplate.valueOf(value)
    } catch (_: Exception) {
        PageTemplate.LINED
    }

    @TypeConverter
    fun fromStrokeList(strokes: List<Stroke>): String {
        val root = JSONArray()
        for (stroke in strokes) {
            val obj = JSONObject()
            obj.put("id", stroke.id)
            obj.put("color", stroke.color)
            obj.put("width", stroke.width.toDouble())
            obj.put("alpha", stroke.alpha.toDouble())
            obj.put("toolType", stroke.toolType.name)
            obj.put("isClosedShape", stroke.isClosedShape)

            val pointsArray = JSONArray()
            for (p in stroke.points) {
                val pt = JSONObject()
                pt.put("x", p.x.toDouble())
                pt.put("y", p.y.toDouble())
                pt.put("pressure", p.pressure.toDouble())
                pointsArray.put(pt)
            }
            obj.put("points", pointsArray)
            root.put(obj)
        }
        return root.toString()
    }

    @TypeConverter
    fun toStrokeList(jsonString: String?): List<Stroke> {
        if (jsonString.isNullOrEmpty()) return emptyList()
        val strokes = mutableListOf<Stroke>()
        try {
            val root = JSONArray(jsonString)
            for (i in 0 until root.length()) {
                val obj = root.getJSONObject(i)
                val id = obj.optString("id", "")
                val color = obj.optLong("color", 0xFF000000)
                val width = obj.optDouble("width", 4.0).toFloat()
                val alpha = obj.optDouble("alpha", 1.0).toFloat()
                val toolName = obj.optString("toolType", ToolType.PEN.name)
                val toolType = try { ToolType.valueOf(toolName) } catch (_: Exception) { ToolType.PEN }
                val isClosedShape = obj.optBoolean("isClosedShape", false)

                val points = mutableListOf<Point>()
                val pointsArray = obj.optJSONArray("points")
                if (pointsArray != null) {
                    for (j in 0 until pointsArray.length()) {
                        val pt = pointsArray.getJSONObject(j)
                        val x = pt.optDouble("x", 0.0).toFloat()
                        val y = pt.optDouble("y", 0.0).toFloat()
                        val pressure = pt.optDouble("pressure", 1.0).toFloat()
                        points.add(Point(x, y, pressure))
                    }
                }
                strokes.add(
                    Stroke(
                        id = id,
                        points = points,
                        color = color,
                        width = width,
                        alpha = alpha,
                        toolType = toolType,
                        isClosedShape = isClosedShape
                    )
                )
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return strokes
    }

    @TypeConverter
    fun fromTextAnnotationList(annotations: List<TextAnnotation>): String {
        val root = JSONArray()
        for (ann in annotations) {
            val obj = JSONObject()
            obj.put("id", ann.id)
            obj.put("text", ann.text)
            obj.put("x", ann.x.toDouble())
            obj.put("y", ann.y.toDouble())
            obj.put("fontSizeSp", ann.fontSizeSp.toDouble())
            obj.put("color", ann.color)
            obj.put("isBold", ann.isBold)
            obj.put("isItalic", ann.isItalic)
            obj.put("width", ann.width.toDouble())
            root.put(obj)
        }
        return root.toString()
    }

    @TypeConverter
    fun toTextAnnotationList(jsonString: String?): List<TextAnnotation> {
        if (jsonString.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<TextAnnotation>()
        try {
            val root = JSONArray(jsonString)
            for (i in 0 until root.length()) {
                val obj = root.getJSONObject(i)
                list.add(
                    TextAnnotation(
                        id = obj.optString("id", ""),
                        text = obj.optString("text", ""),
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        fontSizeSp = obj.optDouble("fontSizeSp", 16.0).toFloat(),
                        color = obj.optLong("color", 0xFF000000),
                        isBold = obj.optBoolean("isBold", false),
                        isItalic = obj.optBoolean("isItalic", false),
                        width = obj.optDouble("width", 300.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return list
    }
}
