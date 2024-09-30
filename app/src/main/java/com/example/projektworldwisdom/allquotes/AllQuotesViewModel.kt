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
    val quotes: LiveData<List<Quote>?> = _quotes

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
        selectedKeywords.observeForever { updateFilteredQuotes() }
        searchKeyword.observeForever { updateFilteredQuotes() }
    }

    fun clearSelectedKeywords() {
        _selectedKeywords.value = emptyList()
    }

    fun updateSelectedKeywords(newKeywords: List<String>) {
        _selectedKeywords.value = newKeywords
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            setLoading(true) // Ladeindikator aktivieren
            try {
                val allQuotes = repository.getAllQuotes() // Alle Zitate aus dem Repository abrufen
                _quotes.postValue(allQuotes) // Zitate im LiveData speichern
                _filteredQuotes.postValue(allQuotes) // Auch gefilterte Zitate setzen (anfangs keine Filterung)
            } catch (e: Exception) {
                handleLoadingError(e) // Fehlerbehandlung bei Problemen
            } finally {
                setLoading(false) // Ladeindikator deaktivieren
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
                val keywords = repository.getAvailableKeywords() // Verfügbare Keywords abrufen
                _availableKeywords.postValue(keywords) // Keywords im LiveData speichern
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Schlüsselwörter", e) // Fehlerprotokollierung
            }
        }
    }

    private fun loadAllAuthors() {
        viewModelScope.launch {
            try {
                val authorsList = repository.getAllAuthors() // Alle Autoren abrufen
                _authors.postValue(authorsList) // Autoren in LiveData speichern
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Autoren", e) // Fehlerprotokollierung
            }
        }
    }

    private fun updateFilteredQuotes() {
        viewModelScope.launch {
            setLoading(true)
            try {
                val author = normalizeString(_searchKeyword.value) // Autorname normalisieren
                val keywords = _selectedKeywords.value ?: emptyList() // Gewählte Keywords abrufen

                Log.d("AllQuotesViewModel", "Filtering with author: '$author', keywords: $keywords")

                // Filtere die Zitate basierend auf Autor und Keywords
                val filteredQuotes = filterQuotes(author, keywords)

                // Entferne Duplikate aus den gefilterten Zitaten
                _filteredQuotes.postValue(filteredQuotes.distinct())

            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                setLoading(false)
            }
        }
    }

    private fun filterQuotes(author: String?, keywords: List<String>): List<Quote> {
        return _quotes.value?.filter { quote ->
            val matchesAuthor = author.isNullOrEmpty() || quote.authorName?.contains(author, ignoreCase = true) == true
            val matchesKeywords = keywords.isEmpty() || quote.keywords.any { keyword -> keywords.contains(keyword) }

            Log.d("FilterQuotes", "Quote: ${quote.authorName}, Matches Author: $matchesAuthor, Matches Keywords: $matchesKeywords")
            matchesAuthor && matchesKeywords
        } ?: emptyList()
    }

    fun filterByKeyword(selectedKeywords: List<String>) {
        _selectedKeywords.value = selectedKeywords
        updateFilteredQuotes() // Stelle sicher, dass die Filterung aktualisiert wird
    }

    fun searchByAuthorAndKeywords(authorName: String?, keywords: List<String>) {
        viewModelScope.launch {
            val filteredQuotes = repository.searchQuotes(authorName, keywords)
            _filteredQuotes.postValue(filteredQuotes) // Gefilterte Zitate zurückgeben
        }
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
}