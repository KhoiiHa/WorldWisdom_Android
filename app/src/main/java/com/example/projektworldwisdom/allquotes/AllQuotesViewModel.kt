package com.example.projektworldwisdom.allquotes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch

class AllQuotesViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchKeyword = MutableLiveData<String>()
    val searchKeyword: LiveData<String> get() = _searchKeyword

    init {
        loadAllQuotes()
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue(null) // Fehlerstatus zurücksetzen

            try {
                val allQuotes = WorldWisdomApi.retrofitService.getAllQuotes()
                repository.insertQuotes(allQuotes)
                _quotes.postValue(allQuotes)
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun filterByKeyword(keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue(null) // Fehlerstatus zurücksetzen

            try {
                val filteredQuotes = if (keywords.isEmpty()) {
                    // Wenn die Liste leer ist, alle Zitate abrufen
                    repository.getAllQuotes()
                } else {
                    // Zitate anhand der angegebenen Schlüsselwörter abrufen
                    repository.getQuotesByKeywords(keywords)
                }
                _quotes.value = filteredQuotes
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByAuthor(authorName: String) {
        _searchKeyword.value = authorName
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue(null) // Fehlerstatus zurücksetzen

            try {
                val searchResults = repository.searchQuotesByAuthor(authorName)
                _quotes.postValue(searchResults)
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun handleLoadingError(e: Exception) {
        // Hier kannst du die Fehlerbehandlung zentralisieren
        val localQuotes = repository.getAllQuotesFromLocal()
        if (localQuotes.isNotEmpty()) {
            _quotes.postValue(localQuotes)
        } else {
            _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
            Log.e("AllQuotesViewModel", "Fehler beim Laden der Zitate", e)
        }
    }
}