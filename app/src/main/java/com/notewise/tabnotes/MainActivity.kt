package com.notewise.tabnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.notewise.tabnotes.data.local.NoteDatabase
import com.notewise.tabnotes.data.repository.NoteRepository
import com.notewise.tabnotes.ui.screens.MainTabletScreen
import com.notewise.tabnotes.ui.theme.NotewiseTabTheme
import com.notewise.tabnotes.viewmodel.LibraryViewModel
import com.notewise.tabnotes.viewmodel.NoteEditorViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { NoteDatabase.getInstance(this) }
    private val repository by lazy { NoteRepository(database.noteDao()) }

    private val editorViewModel: NoteEditorViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NoteEditorViewModel(repository) as T
            }
        }
    }

    private val libraryViewModel: LibraryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LibraryViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotewiseTabTheme {
                MainTabletScreen(
                    editorViewModel = editorViewModel,
                    libraryViewModel = libraryViewModel
                )
            }
        }
    }
}
