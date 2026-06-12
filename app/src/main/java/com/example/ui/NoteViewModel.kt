package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val categories = listOf("All", "General", "Work", "Personal", "Ideas", "Todos")

    val softColors = listOf(
        NoteColor("Classic", 0xFFFFFFFF),
        NoteColor("Yellow", 0xFFFEF3C7),
        NoteColor("Green", 0xFFDCFCE7),
        NoteColor("Blue", 0xFFE0F2FE),
        NoteColor("Lavender", 0xFFF3E8FF),
        NoteColor("Pink", 0xFFFCE7F3),
        NoteColor("Orange", 0xFFFFEDD5),
        NoteColor("Slate", 0xFFF1F5F9)
    )

    val notesListState: StateFlow<List<Note>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        val rawFlow = if (query.isBlank()) {
            repository.allNotes
        } else {
            repository.searchNotes(query)
        }
        rawFlow.map { list ->
            if (category == "All") {
                list
            } else {
                list.filter { it.category.equals(category, ignoreCase = true) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun saveNote(
        id: Int? = null,
        title: String,
        content: String,
        category: String,
        colorHex: Long,
        isPinned: Boolean = false,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            if (title.isBlank() && content.isBlank()) {
                return@launch
            }
            val cleanTitle = title.trim()
            val cleanContent = content.trim()
            val cleanCategory = if (category.isBlank()) "General" else category.trim()

            if (id == null || id == 0) {
                val newNote = Note(
                    title = cleanTitle,
                    content = cleanContent,
                    category = cleanCategory,
                    colorHex = colorHex,
                    isPinned = isPinned
                )
                repository.insertNote(newNote)
            } else {
                val existingNote = repository.getNoteById(id)
                if (existingNote != null) {
                    val updatedNote = existingNote.copy(
                        title = cleanTitle,
                        content = cleanContent,
                        category = cleanCategory,
                        colorHex = colorHex,
                        isPinned = isPinned
                    )
                    repository.updateNote(updatedNote)
                }
            }
            onComplete()
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(isPinned = !note.isPinned)
            repository.updateNote(updated)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }
}

data class NoteColor(val name: String, val colorLong: Long)

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
