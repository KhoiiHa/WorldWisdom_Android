package com.example.projektworldwisdom.allquotes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch

class AllQuotesViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _authors = MutableLiveData<List<Author>>()
    val authors: LiveData<List<Author>> = _authors

    private val _filteredQuotes = MutableLiveData<List<Quote>>(emptyList()) // Setze leere Liste als Standard
    val filteredQuotes: LiveData<List<Quote>> = _filteredQuotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchKeyword = MutableLiveData<String>()
    val searchKeyword: LiveData<String> = _searchKeyword

    private val _selectedKeywords = MutableLiveData<List<String>>()
    val selectedKeywords: LiveData<List<String>> = _selectedKeywords

    private val _availableKeywords = MutableLiveData<List<String>>()
    val availableKeywords: LiveData<List<String>> = _availableKeywords

    init {
        loadAllQuotes()
        loadAvailableKeywords()
        loadAllAuthors()
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue(null)

            try {
                val allQuotes = repository.getAllQuotes()
                _quotes.postValue(allQuotes)
                _filteredQuotes.postValue(allQuotes) // Synchronisiere auch die gefilterten Zitate
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun loadAllAuthors() {
        viewModelScope.launch {
            try {
                val authorsList = repository.getAllAuthors()
                _authors.postValue(authorsList)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Autoren", e)
            }
        }
    }

    fun filterByKeyword(keywords: List<String>) {
        _selectedKeywords.postValue(keywords)
        updateFilteredQuotes()
    }

    fun searchByAuthor(authorName: String) {
        _searchKeyword.postValue(authorName)
        updateFilteredQuotes()
    }

    private fun updateFilteredQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue(null)

            try {
                val author = _searchKeyword.value ?: ""
                val keywords = _selectedKeywords.value ?: emptyList()

                val filteredQuotes = when {
                    author.isNotEmpty() -> repository.searchQuotesByAuthor(author)
                    keywords.isNotEmpty() -> repository.getQuotesByKeywords(keywords)
                    else -> repository.getAllQuotes()
                }

                _filteredQuotes.postValue(filteredQuotes) // Aktualisiere gefilterte Zitate
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun loadAvailableKeywords() {
        viewModelScope.launch {
            try {
                val keywords = repository.getAvailableKeywords()
                _availableKeywords.postValue(keywords)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Schlüsselwörter", e)
            }
        }
    }

    private suspend fun handleLoadingError(e: Exception) {
        val localQuotes = repository.getAllQuotesFromLocal()
        if (localQuotes.isNotEmpty()) {
            _quotes.postValue(localQuotes)
            _filteredQuotes.postValue(localQuotes) // Aktualisiere auch gefilterte Zitate bei Fehler
        } else {
            _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
            Log.e("AllQuotesViewModel", "Fehler beim Laden der Zitate", e)
        }
    }
}