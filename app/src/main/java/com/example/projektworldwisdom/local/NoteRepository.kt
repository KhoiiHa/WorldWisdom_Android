package com.example.projektworldwisdom.local


import android.util.Log
import com.example.projektworldwisdom.model.Note

class NoteRepository(private val noteDao: NoteDao) {

    suspend fun insertNote(note: Note) {
        try {
            noteDao.insertNote(note)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Fehler beim Einfügen der Notiz: ${e.message}")
        }
    }

    suspend fun deleteNote(note: Note) {
        try {
            noteDao.delete(note)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Fehler beim Löschen der Notiz: ${e.message}")
        }
    }

    suspend fun deleteAllNotes() {
        try {
            noteDao.deleteAllNotes()
        } catch (e: Exception) {
            Log.e("NoteRepository", "Fehler beim Löschen aller Notizen: ${e.message}")
        }
    }

    suspend fun getAllNotes(): List<Note> {
        return try {
            noteDao.getAllNotes()
        } catch (e: Exception) {
            Log.e("NoteRepository", "Fehler beim Abrufen aller Notizen: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return try {
            noteDao.getNoteById(id)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Fehler beim Abrufen der Notiz mit ID $id: ${e.message}", e)
            null
        }
    }
}