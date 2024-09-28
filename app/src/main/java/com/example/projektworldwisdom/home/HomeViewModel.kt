package com.example.projektworldwisdom.home

import android.util.Log
import androidx.lifecycle.*
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>?>()
    val quotes: LiveData<List<Quote>?> = _quotes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _dailyAffirmation = MutableLiveData<Quote?>()
    val dailyAffirmation: LiveData<Quote?> = _dailyAffirmation

    private val _authors = MutableLiveData<List<Author>?>()
    val authors: LiveData<List<Author>?> = _authors

    init {
        loadQuoteOfTheDay() // Lade das Zitat des Tages
        loadAllQuotesHome() // Lade alle Zitate
        loadAllAuthors() // Lade alle Autoren
    }

    fun clearError() {
        _error.value = null
    }

    fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quote = repository.getQuoteOfTheDay() ?: repository.getQuoteOfTheDayFromLocal()
                _dailyAffirmation.postValue(quote)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden des Zitats: ${e.message}")
                _dailyAffirmation.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadAllQuotesHome() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val allQuotes = repository.getAllQuotes()
                Log.d("HomeViewModel", "Fetched quotes: $allQuotes")

                if (allQuotes.isEmpty()) {
                    _error.postValue("Keine Zitate gefunden.")
                } else {
                    _quotes.postValue(allQuotes)
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Neue Methode zum Speichern eines Zitats
    fun saveQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.saveQuote(quote) // Aufruf der Methode im Repository, um das Zitat zu speichern
                Log.d("HomeViewModel", "Zitat erfolgreich gespeichert: $quote")
            } catch (e: Exception) {
                _error.postValue("Fehler beim Speichern des Zitats: ${e.message}")
            }
        }
    }

    fun searchByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quotes = repository.getQuotesByTag(tag)
                handleQuotesResponse(quotes, tag)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchQuotes(query: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quotes = repository.getQuotesByQuery(query)
                handleQuotesResponse(quotes, query)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadAllAuthors() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val allAuthors = repository.getAllAuthors()
                if (allAuthors.isEmpty()) {
                    _error.postValue("Keine Autoren gefunden.")
                } else {
                    _authors.postValue(allAuthors)
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Autoren: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByAuthorAndKeywords(authorName: String, keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quotes = repository.getQuotesByKeywords(keywords)
                handleQuotesResponse(quotes, authorName)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getAuthorByName(authorName: String): LiveData<Author?> {
        val authorLiveData = MutableLiveData<Author?>()

        if (authorName.isBlank()) {
            authorLiveData.postValue(null)
            return authorLiveData
        }

        viewModelScope.launch {
            try {
                val author = repository.getAuthorByName(authorName)
                authorLiveData.postValue(author)
            } catch (e: Exception) {
                authorLiveData.postValue(null)
                Log.e("HomeViewModel", "Fehler beim Abrufen des Autors: ${e.message}")
            }
        }
        return authorLiveData
    }

    fun loadQuotesByKeywords(keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                // Rufe Zitate basierend auf den Keywords ab
                val quotes = repository.getQuotesByKeywords(keywords)
                handleQuotesResponse(quotes, keywords.joinToString(", "))
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun handleQuotesResponse(quotes: List<Quote>?, searchQuery: String) {
        if (quotes.isNullOrEmpty()) {
            Log.d("ViewModel", "Keine Zitate für '$searchQuery' gefunden.")
            _error.postValue("Keine Zitate für '$searchQuery' gefunden.")
        } else {
            Log.d("ViewModel", "Gefundene Zitate für '$searchQuery': $quotes")
            _quotes.postValue(quotes)
            _error.postValue(null) // Fehlermeldung zurücksetzen
        }
    }
}