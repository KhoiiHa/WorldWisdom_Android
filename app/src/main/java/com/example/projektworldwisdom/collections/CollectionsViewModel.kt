package com.example.projektworldwisdom.collections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class CollectionsViewModel(private val repository: QuoteRepository) : ViewModel() {

    // LiveData, um die gespeicherten Zitate zu beobachten
    val savedQuotes: LiveData<List<Quote>> = repository.getSavedQuotes()

    private val _commentAddedSuccessfully = MutableLiveData<Boolean>()
    val commentAddedSuccessfully: LiveData<Boolean> = _commentAddedSuccessfully

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun addCommentToQuote(quote: Quote, newComment: String) {
        viewModelScope.launch {
            try {
                // Füge den neuen Kommentar zur Liste hinzu, falls der Kommentar nicht leer ist
                if (newComment.isNotBlank()) {
                    val updatedComments = quote.comments + newComment // Füge den neuen Kommentar hinzu
                    val updatedQuote = quote.copy(comments = updatedComments) // Erstelle ein neues Quote-Objekt mit aktualisierten Kommentaren
                    repository.updateQuote(updatedQuote) // Aktualisiere das Zitat im Repository
                    // Nach dem Hinzufügen eines Kommentars wird die Liste automatisch aktualisiert
                    _commentAddedSuccessfully.value = true
                } else {
                    _error.postValue("Kommentar darf nicht leer sein.")
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Speichern des Kommentars: ${e.message}")
            }
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.deleteQuote(quote)
                // Nach dem Löschen wird die Liste automatisch aktualisiert
            } catch (e: Exception) {
                _error.postValue("Fehler beim Löschen des Zitats: ${e.message}")
            }
        }
    }

    fun resetCommentAddedSuccessfully() {
        _commentAddedSuccessfully.value = false
    }
}