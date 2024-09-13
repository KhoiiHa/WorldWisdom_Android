package com.example.projektworldwisdom.local


import com.example.projektworldwisdom.model.Note

class NoteRepository(private val noteDao: NoteDao) {

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }
}