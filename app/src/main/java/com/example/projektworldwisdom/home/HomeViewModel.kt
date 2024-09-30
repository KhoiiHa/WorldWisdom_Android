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

    // MutableLiveData für Zitate
    private val _quotes = MutableLiveData<List<Quote>?>()
    val quotes: LiveData<List<Quote>?> = _quotes

    // MutableLiveData für Autoren
    private val _authors = MutableLiveData<List<Author>?>()
    val authors: LiveData<List<Author>?> = _authors

    // MutableLiveData für Tags
    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags

    // MutableLiveData für Keywords
    private val _keywords = MutableLiveData<List<String>>()
    val keywords: LiveData<List<String>> get() = _keywords

    // MutableLiveData für Ladezustand
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // MutableLiveData für Fehler
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // MutableLiveData für das Zitat des Tages
    private val _dailyAffirmation = MutableLiveData<Quote?>()
    val dailyAffirmation: LiveData<Quote?> = _dailyAffirmation

    // MutableLiveData für Autocomplete-Vorschläge
    private val _autocompleteSuggestions = MutableLiveData<List<String>>()
    val autocompleteSuggestions: LiveData<List<String>> get() = _autocompleteSuggestions

    // Initialisierung: Lade Daten beim Start des ViewModels
    init {
        loadQuoteOfTheDay() // Lade das Zitat des Tages
        loadAllQuotesHome() // Lade alle Zitate für die Startseite
        loadAllAuthors() // Lade alle Autoren
        loadAllTags() // Lade alle Tags für Autocomplete
        loadAllKeywords() // Lade alle Keywords für Autocomplete
    }

    // Funktion zum Zurücksetzen von Fehlern
    fun clearError() {
        _error.value = null
    }

    // Lade das Zitat des Tages
    fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quote = repository.getQuoteOfTheDay() ?: repository.getQuoteOfTheDayFromLocal()
                _dailyAffirmation.postValue(quote)
            } catch (e: Exception) {
                handleError("Fehler beim Laden des Zitats: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Lade alle Zitate für die Startseite
    fun loadAllQuotesHome() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val allQuotes = repository.getAllQuotes()
                if (allQuotes.isEmpty()) {
                    handleError("Keine Zitate gefunden.")
                } else {
                    _quotes.postValue(allQuotes)
                }
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Speichere ein Zitat
    fun saveQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.saveQuote(quote)
            } catch (e: Exception) {
                handleError("Fehler beim Speichern des Zitats: ${e.message}")
            }
        }
    }

    // Suche Zitate anhand eines Tags
    fun searchByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quotes = repository.getQuotesByTag(tag)
                handleQuotesResponse(quotes, tag)
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Suche Zitate basierend auf Autorennamen und Keywords
    fun searchQuotes(authorName: String?, keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quotes = repository.searchQuotes(authorName, keywords)
                handleQuotesResponse(quotes, authorName ?: "Suchanfrage") // Füge die Sucheingabe als Kontext hinzu
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Lade alle Autoren
    fun loadAllAuthors() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val allAuthors = repository.getAllAuthors()
                if (allAuthors.isEmpty()) {
                    handleError("Keine Autoren gefunden.")
                } else {
                    _authors.postValue(allAuthors)
                }
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Autoren: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Suche Zitate basierend auf Autorennamen und Keywords
    fun searchByAuthorAndKeywords(authorName: String?, keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeindikator aktivieren
            clearError() // Vorherige Fehler löschen

            try {
                // Aufruf der Repository-Methode zur Suche nach Zitaten
                val quotes = repository.searchQuotes(authorName, keywords)
                if (authorName != null) {
                    handleQuotesResponse(quotes, authorName)
                } // Verarbeite die erhaltenen Zitate
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Zitate: ${e.message}") // Fehlerbehandlung
            } finally {
                _isLoading.postValue(false) // Ladeindikator deaktivieren
            }
        }
    }

    // Lade einen Autor anhand seines Namens
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

    // Lade Zitate basierend auf einer Liste von Keywords
    fun loadQuotesByKeywords(keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val quotes = repository.getQuotesByKeywords(keywords)
                handleQuotesResponse(quotes, keywords.joinToString(", "))
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
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

    // Autocomplete für Suchvorschläge nach Autoren, Tags und Keywords
    fun searchAutocomplete(query: String) {
        viewModelScope.launch {
            clearError()
            _isLoading.postValue(true)

            try {
                // Abrufen der Daten
                val authors = repository.getAllAuthors() // List<Author>
                val tags = repository.getAllTags() // List<String>
                val keywords = repository.getAllKeywords() // List<String>

                // Filtere die Autoren nach dem Namen
                val authorSuggestions = authors
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .map { it.name }

                // Filtere die Tags nach dem Tag-Namen
                val tagSuggestions = tags.filter { it.contains(query, ignoreCase = true) }

                // Filtere die Keywords nach dem Keyword
                val keywordSuggestions = keywords.filter { it.contains(query, ignoreCase = true) }

                // Kombiniere alle Vorschläge und entferne Duplikate
                val combinedSuggestions = (authorSuggestions + tagSuggestions + keywordSuggestions).distinct()

                // Posten der kombinierten Vorschläge in LiveData
                _autocompleteSuggestions.postValue(combinedSuggestions)
            } catch (e: Exception) {
                _autocompleteSuggestions.postValue(emptyList())
                handleError("Fehler beim Abrufen von Autocomplete-Vorschlägen: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Lade alle Tags für Autocomplete
    private fun loadAllTags() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                val allTags = repository.getAllTags()
                _tags.postValue(allTags)
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Tags: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Lade alle Keywords für Autocomplete
    private fun loadAllKeywords() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            try {
                // Holen Sie sich alle Schlüsselwörter als Liste von Strings
                val allKeywords = repository.getAllKeywords()
                _keywords.postValue(allKeywords) // Angenommen, _keywords ist vom Typ MutableLiveData<List<String>>
            } catch (e: Exception) {
                handleError("Fehler beim Abrufen der Keywords: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Zentrale Fehlerbehandlungsfunktion
    private fun handleError(message: String) {
        _error.postValue(message)
        Log.e("HomeViewModel", message)
    }
}