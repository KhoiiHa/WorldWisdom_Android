package com.example.projektworldwisdom.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projektworldwisdom.model.Note

@Dao
interface NoteDao {

    // Einfügen eines einzelnen Notiz
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    // Abrufen aller Notizen
    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    // Abrufen einer Notiz nach ID
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    // Löschen einer Notiz
    @Delete
    suspend fun delete(note: Note)

    // Löschen aller Notizen
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}