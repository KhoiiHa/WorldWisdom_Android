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
                repository.updateQuote(updatedQuote)
                loadSavedQuotes()
                _commentAddedSuccessfully.value = true
            } catch (e: Exception) {
                _error.postValue("Fehler beim Speichern des Kommentars: ${e.message}")
            }
        }
    }

    fun resetCommentAddedSuccessfully() {
        _commentAddedSuccessfully.value = false
    }
}