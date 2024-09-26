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

    private val _author = MutableLiveData<String?>()
    val author: LiveData<String?> = _author

    private val _dailyAffirmation = MutableLiveData<Quote?>()
    val dailyAffirmation: LiveData<Quote?> = _dailyAffirmation

    private val _keywords = MutableLiveData<List<Keyword>>()
    val keywords: LiveData<List<Keyword>> = _keywords

    // Neu: LiveData für die Autoren
    private val _authors = MutableLiveData<List<Author>?>()
    val authors: LiveData<List<Author>?> = _authors

    fun clearError() {
        _error.value = null
    }

    init {
        loadQuoteOfTheDay() // Initiales Laden des Zitats des Tages
        loadAllQuotesHome() // Initiales Laden aller Zitate
        loadAllAuthors() // Neu: Autoren laden
    }

    fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError() // Fehlerstatus zurücksetzen

            try {
                val quote = repository.getQuoteOfTheDay() ?: repository.getQuoteOfTheDayFromLocal()
                _dailyAffirmation.postValue(quote)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden des Zitats: ${e.message}")
                _dailyAffirmation.postValue(null)
            } finally {
                _isLoading.postValue(false) // Ladezustand zurücksetzen
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

    // Neu: Funktion zum Laden aller Autoren
    fun loadAllAuthors() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError() // Fehlerstatus zurücksetzen

            try {
                val allAuthors = repository.getAllAuthors() // Funktion aus dem Repository verwenden
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

    private fun handleQuotesResponse(quotes: List<Quote>, searchQuery: String) {
        if (quotes.isEmpty()) {
            _error.postValue("Keine Zitate für '$searchQuery' gefunden.")
        } else {
            _quotes.postValue(quotes)
            _error.postValue(null) // Fehlermeldung zurücksetzen
        }
    }

    fun loadQuotesByKeywords(keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // Abrufen der Zitate von der Repository unter Verwendung der Liste von Keywords
                val quotes = repository.getQuotesByKeywords(keywords)
                // Übergabe der Zitate und der Keywords an die Funktion
                handleQuotesResponse(quotes, keywords.joinToString(", ")) // Verwendung der Keywords als String
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByAuthor(authorName: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            // Überprüfen, ob der Autorname gültig ist
            if (authorName.isBlank()) {
                _error.postValue("Bitte geben Sie einen gültigen Autorennamen ein.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                // Suche nach Zitaten des Autors
                val quotes = repository.searchQuotesByAuthor(authorName)
                // Überprüfen, ob Zitate gefunden wurden
                if (quotes.isNotEmpty()) {
                    _quotes.postValue(quotes)
                } else {
                    // Wenn keine Zitate gefunden wurden, versuche lokale Zitate zu laden
                    val localQuotes = repository.getQuotesByAuthor(authorName)
                    handleQuotesResponse(localQuotes, authorName)
                }
                // Fehlermeldung zurücksetzen
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für den Autor: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByKeyword(keyword: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            if (keyword.isBlank()) {
                _error.postValue("Bitte geben Sie ein gültiges Schlüsselwort ein.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                // Hier wird das Keyword in eine Liste umgewandelt, um es der Funktion zu übergeben
                val quotes = repository.getQuotesByKeywords(listOf(keyword))
                handleQuotesResponse(quotes, keyword) // Verwende nur die Zitate, ohne das Keyword zu übergeben
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für das Schlüsselwort: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            if (tag.isBlank()) {
                _error.postValue("Bitte geben Sie ein gültiges Tag ein.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                // Suche nach Zitaten anhand des Tags
                val quotes = repository.getQuotesByTag(tag)
                handleQuotesResponse(quotes, tag)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für das Tag: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchQuotes(query: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            if (query.isBlank()) {
                _error.postValue("Bitte geben Sie einen gültigen Suchbegriff ein.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                // Suche nach Zitaten des Autors
                val quotesByAuthor = repository.searchQuotesByAuthor(query)
                // Suche nach Zitaten mit dem Keyword
                val quotesByKeyword = repository.getQuotesByKeywords(listOf(query))
                // Suche nach Zitaten mit dem Tag
                val quotesByTag = repository.getQuotesByTag(query)

                // Kombinierte Zitate (je nach deiner Logik hier)
                val combinedQuotes = (quotesByAuthor + quotesByKeyword + quotesByTag).distinct()

                handleQuotesResponse(combinedQuotes, query)
            } catch (e: Exception) {
                _error.postValue("Fehler bei der Suche nach Zitaten: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getAuthorByName(authorName: String): LiveData<Author?> {
        // LiveData für den Autor initialisieren
        val authorLiveData = MutableLiveData<Author?>()

        // Überprüfen, ob der Autorname gültig ist
        if (authorName.isBlank()) {
            authorLiveData.postValue(null)
            return authorLiveData
        }

        viewModelScope.launch {
            try {
                // Abrufen des Autors aus dem Repository
                val author = repository.getAuthorByName(authorName)
                authorLiveData.postValue(author)
            } catch (e: Exception) {
                // Bei einem Fehler null zurückgeben und Fehler protokollieren
                authorLiveData.postValue(null)
                Log.e("HomeViewModel", "Fehler beim Abrufen des Autors: ${e.message}")
            }
        }
        return authorLiveData
    }
}