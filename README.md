# Notewise Tab - Android Tablet Note-Taking Application

A digital handwriting and note-taking application designed for **Android Tablets**, inspired by the architecture and capabilities of **Notewise**.

---

## 🌟 Key Features

### 1. 📑 Tablet-Optimized Multi-Document Tab System
- **Tabbed Interface:** Keep multiple notebooks open simultaneously in top horizontal tabs.
- **Fast Tab Switching:** Seamlessly jump between notes during lectures, meetings, or research without losing your place or returning to the library.
- **Dynamic Tabs:** Open, reorder, close tabs, or tap `+` to spin up a new notebook tab instantly.
- **Page-State Memory:** Each tab remembers its current page index and scroll context.

### 2. ✍️ Fluid Low-Latency Stylus & Handwriting Engine
- **Quadratic Bézier Smoothing:** Eliminates angular jitter, rendering fluid, natural handwritten ink lines.
- **Tool Spectrum:**
  - **Ballpoint Pen:** Fine, consistent lines for quick notes.
  - **Fountain Pen:** Dynamic, pressure-modulated calligraphy strokes.
  - **Highlighter:** Semi-translucent ink with customizable alpha blending so underlying notes remain legible.
  - **Stroke & Point Eraser:** Fast spatial collision detection to wipe away unwanted strokes.
  - **Text Annotations:** Typewritten text boxes alongside handwritten diagrams.
- **Palm Rejection / Stylus-Only Mode:** Disables touch drawing so your resting hand won't leave stray marks while using a stylus.

### 3. 📐 Professional Page Templates & Paper Tones
- **Templates:**
  - **Ruled / Lined:** Standard ruled lines with vertical margin guide.
  - **Grid / Graph:** 5mm / 10mm graph paper for mathematics and schematics.
  - **Dot Matrix:** Subtle dot grids for bullet journaling.
  - **Cornell Notes:** Dedicated cue column, notes body, and bottom summary divider.
  - **Task Planner:** Checkbox lists and ruled task dividers.
  - **Blank Paper:** Clean canvas for unrestrained sketches and mind maps.
- **Paper Backgrounds:** Pure White, Warm Ivory, Vintage Sepia, Graphite Dark, and Midnight Blue.

### 4. 🗄️ Offline-First Room Architecture
- **Automatic Debounced Persistence:** Strokes, annotations, and page updates save in the background to local SQLite via Room.
- **Undo / Redo Stack:** Unlimited page-level undo and redo.
- **Library Organization:** Notebook covers with spine styling, folders, search filtering, and starred/favorite notes.

---

## 🏗️ Project Architecture

```
app/src/main/java/com/notewise/tabnotes/
├── MainActivity.kt                      # Edge-to-edge tablet entry point
├── data/
│   ├── local/
│   │   ├── Converters.kt                # JSON TypeConverters for Strokes & Annotations
│   │   ├── NoteDao.kt                   # Room DAOs for Notebooks, Pages & Folders
│   │   └── NoteDatabase.kt              # Room SQLite database instance
│   ├── model/
│   │   ├── Notebook.kt                  # Notebook and Folder entities
│   │   ├── NotePage.kt                  # NotePage entity with template & color
│   │   ├── PageTemplate.kt              # Template and Paper color enums
│   │   ├── Stroke.kt                    # Vector ink stroke & Bézier point models
│   │   ├── TextAnnotation.kt            # Typewritten note blocks
│   │   └── ToolType.kt                  # Pen, Fountain, Highlighter, Eraser, Text
│   └── repository/
│       └── NoteRepository.kt            # Repository coordinating data & ViewModels
├── ui/
│   ├── components/
│   │   ├── InkCanvas.kt                 # Stylus & handwriting canvas with Bézier curves
│   │   ├── NavigationRail.kt            # Tablet left navigation sidebar
│   │   ├── NoteTabBar.kt                # Horizontal document tab bar
│   │   ├── PageNavigator.kt             # Bottom floating page switcher (e.g. Page 2 / 5)
│   │   ├── PageTemplateCanvas.kt        # Ruled, Grid, Dot, Cornell paper renderer
│   │   └── StylusToolbar.kt             # Tools, color chips, thickness slider & templates
│   ├── screens/
│   │   ├── LibraryScreen.kt             # Notebook gallery, folders, and creation modal
│   │   └── MainTabletScreen.kt          # Dual-pane tablet workspace container
│   └── theme/
│       ├── Color.kt                     # Notewise cobalt blue & palette presets
│       ├── Theme.kt                     # Material 3 adaptive light/dark theme
│       └── Type.kt                      # Typography scale
└── viewmodel/
    ├── LibraryViewModel.kt              # Notebook gallery & folder management
    └── NoteEditorViewModel.kt           # Tab management, active canvas, undo/redo & saving
```

---

## 🚀 How to Build and Run

### Prerequisites
- **Android Studio** (Ladybug / Koala or newer recommended)
- **JDK 17** or **JDK 21**
- **Android SDK Platform 35**

### Method 1: In Android Studio
1. Open Android Studio.
2. Select **File -> Open...** and choose the `vibe coding` directory.
3. Allow Gradle to sync dependencies automatically.
4. Select an **Android Tablet Emulator** (e.g., Pixel Tablet or 10-inch Tablet) or connect a physical Android tablet.
5. Click **Run (`Shift + F10`)**.

### Method 2: Command Line (Gradle)
```bash
# On Windows
gradlew.bat assembleDebug

# On macOS / Linux
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

To install directly to a connected Android tablet or emulator:
```bash
./gradlew installDebug
```
