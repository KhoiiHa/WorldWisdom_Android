package com.example.projektworldwisdom.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projektworldwisdom.model.Note

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?
}