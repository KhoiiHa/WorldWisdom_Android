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

    private val _quotes = MutableLiveData<List<Quote>?>(emptyList())
    val quotes: MutableLiveData<List<Quote>?> = _quotes

    private val _filteredQuotes = MutableLiveData<List<Quote>>(emptyList())
    val filteredQuotes: LiveData<List<Quote>> = _filteredQuotes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchKeyword = MutableLiveData<String>()
    val searchKeyword: LiveData<String> = _searchKeyword

    private val _selectedKeywords = MutableLiveData<List<String>>(emptyList())
    val selectedKeywords: LiveData<List<String>> = _selectedKeywords

    private val _availableKeywords = MutableLiveData<List<String>>(emptyList())
    val availableKeywords: LiveData<List<String>> = _availableKeywords

    private val _authors = MutableLiveData<List<Author>>(emptyList())
    val authors: LiveData<List<Author>> = _authors

    init {
        loadAllQuotes()
        loadAvailableKeywords()
        loadAllAuthors()

        // Beobachte Änderungen an den ausgewählten Keywords und aktualisiere gefilterte Zitate
        _selectedKeywords.observeForever { updateFilteredQuotes() }
        _searchKeyword.observeForever { updateFilteredQuotes() }
    }

    fun clearSelectedKeywords() {
        _selectedKeywords.value = emptyList()
    }

    fun updateSelectedKeywords(newKeywords: List<String>) {
        _selectedKeywords.value = newKeywords
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            setLoading(true)
            try {
                val allQuotes = repository.getAllQuotes()
                _quotes.postValue(allQuotes)
                _filteredQuotes.postValue(allQuotes)
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                setLoading(false)
            }
        }
    }

    fun saveQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.saveQuote(quote)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Speichern des Zitats", e)
            }
        }
    }

    fun loadAvailableKeywords() {
        viewModelScope.launch {
            try {
                val keywords = repository.getAvailableKeywords()
                _availableKeywords.postValue(keywords)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Schlüsselwörter", e)
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

    private fun updateFilteredQuotes() {
        viewModelScope.launch {
            setLoading(true)
            try {
                val author = normalizeString(_searchKeyword.value)
                val keywords = _selectedKeywords.value ?: emptyList()

                Log.d("AllQuotesViewModel", "Filtering with author: '$author', keywords: $keywords")

                val filteredQuotes = _quotes.value?.filter { quote ->
                    val matchesAuthor = author.isEmpty() || quote.authorName?.contains(author, ignoreCase = true) == true
                    val matchesKeywords = keywords.isEmpty() || keywords.any { keyword -> quote.keywords.contains(keyword) }

                    matchesAuthor && matchesKeywords // Beide Bedingungen müssen erfüllt sein
                } ?: emptyList()

                _filteredQuotes.postValue(filteredQuotes)
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                setLoading(false)
            }
        }
    }

    fun filterByKeyword(selectedKeywords: List<String>) {
        _selectedKeywords.value = selectedKeywords
        updateFilteredQuotes() // Stelle sicher, dass die Filterung aktualisiert wird
    }

    fun searchByAuthorAndKeywords(authorName: String?, keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeindikator aktivieren
            clearError() // Vorherige Fehler löschen

            try {
                // Aufruf der Repository-Methode zur Suche nach Zitaten
                val quotes = repository.searchQuotesByAuthorAndKeywords(authorName, keywords)
                if (authorName != null) {
                    handleQuotesResponse(quotes, authorName)
                }
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Zitate: ${e.message}") // Fehlerbehandlung
            } finally {
                _isLoading.postValue(false) // Ladeindikator deaktivieren
            }
        }
    }

    private suspend fun handleLoadingError(e: Exception) {
        val localQuotes = repository.getAllQuotesFromLocal()
        if (localQuotes.isNotEmpty()) {
            _quotes.postValue(localQuotes)
            _filteredQuotes.postValue(localQuotes)
        } else {
            _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
            Log.e("AllQuotesViewModel", "Fehler beim Laden der Zitate", e)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    private fun normalizeString(input: String?): String {
        return input?.trim()?.lowercase() ?: ""
    }

    // Funktion zum Zurücksetzen von Fehlern
    fun clearError() {
        _error.value = null
    }

    // Zentrale Fehlerbehandlungsfunktion
    private fun handleError(message: String) {
        _error.postValue(message)
        Log.e("HomeViewModel", message)
    }

    // Hilfsfunktion zur Verarbeitung der Zitate-Antwort
    private fun handleQuotesResponse(quotes: List<Quote>?, searchQuery: String) {
        if (quotes.isNullOrEmpty()) {
            handleError("Keine Zitate für '$searchQuery' gefunden.")
        } else {
            _quotes.postValue(quotes)
            _error.postValue(null)
        }
    }
}