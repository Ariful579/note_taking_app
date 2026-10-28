package com.notewise.tabnotes.data.model

enum class PageTemplate(val displayName: String) {
    BLANK("Blank Paper"),
    LINED("Lined / Ruled"),
    GRID("Grid / Graph"),
    DOTTED("Dot Matrix"),
    CORNELL("Cornell Notes"),
    TASK_LIST("Task Planner")
}

enum class PaperColor(val hex: Long, val displayName: String) {
    WHITE(0xFFFFFFFF, "Pure White"),
    IVORY(0xFFFAF7EE, "Warm Ivory"),
    SEPIA(0xFFF4ECD8, "Vintage Sepia"),
    DARK_GRAPHITE(0xFF22252A, "Graphite Dark"),
    MIDNIGHT(0xFF14181F, "Midnight Blue")
}
