package com.example.projektworldwisdom.collections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class CollectionsViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _savedQuotes = MutableLiveData<List<Quote>>()
    val savedQuotes: LiveData<List<Quote>> = _savedQuotes

    private val _commentAddedSuccessfully = MutableLiveData<Boolean>()
    val commentAddedSuccessfully: LiveData<Boolean> = _commentAddedSuccessfully

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadSavedQuotes()
    }

    private fun loadSavedQuotes() {
        viewModelScope.launch {
            try {
                // Verwende die vorhandene Funktion, um die gespeicherten Zitate zu laden
                val quotes = repository.getSavedQuotes()
                _savedQuotes.postValue(quotes)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der gespeicherten Zitate: ${e.message}")
            }
        }
    }

    fun addCommentToQuote(quote: Quote, newComment: String) {
        viewModelScope.launch {
            try {
                val updatedQuote = quote.copy(comments = newComment)
                repository.updateQuote(updatedQuote) // Stelle sicher, dass diese Methode im Repository definiert ist
                loadSavedQuotes() // Lade die gespeicherten Zitate nach dem Hinzufügen eines Kommentars neu
                _commentAddedSuccessfully.value = true
            } catch (e: Exception) {
                _error.postValue("Fehler beim Speichern des Kommentars: ${e.message}")
            }
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.deleteQuote(quote)
                loadSavedQuotes() // Aktualisiere die Liste nach dem Löschen
            } catch (e: Exception) {
                _error.postValue("Fehler beim Löschen des Zitats: ${e.message}")
            }
        }
    }

    fun resetCommentAddedSuccessfully() {
        _commentAddedSuccessfully.value = false
    }
}