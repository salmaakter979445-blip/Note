package com.quicknotes.app

import android.app.Application
import com.quicknotes.app.data.NoteDatabase
import com.quicknotes.app.data.NoteRepository

class NoteApplication : Application() {
    val database by lazy { NoteDatabase.getDatabase(this) }
    val repository by lazy { NoteRepository(database.noteDao) }
}
