package com.example.projektworldwisdom.Note

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.local.NoteRepository
import com.example.projektworldwisdom.model.Note
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _noteContent = MutableLiveData<String>()
    val noteContent: LiveData<String> get() = _noteContent

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadNoteContent(noteId: Int) {
        viewModelScope.launch {
            try {
                val note = repository.getNoteById(noteId)
                _noteContent.value = note?.content ?: ""
                _error.value = null // Fehler zurücksetzen, falls vorhanden
            } catch (e: Exception) {
                _error.value = "Fehler beim Laden der Notiz: ${e.message}"
                Log.e("NoteViewModel", "Error loading note", e)
            }
        }
    }

    fun saveNoteContent(noteId: Int, content: String) {
        viewModelScope.launch {
            try {
                repository.insertNote(Note(id = noteId, content = content))
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Fehler beim Speichern der Notiz: ${e.message}"
                Log.e("NoteViewModel", "Error saving note", e)
            }
        }
    }
}