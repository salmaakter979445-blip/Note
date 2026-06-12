package com.example

import android.app.Application
import com.example.data.NoteDatabase
import com.example.data.NoteRepository

class NoteApplication : Application() {
    val database by lazy { NoteDatabase.getDatabase(this) }
    val repository by lazy { NoteRepository(database.noteDao) }
}
