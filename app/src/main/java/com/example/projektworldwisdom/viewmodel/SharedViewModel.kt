package com.example.projektworldwisdom.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.local.AppDatabase
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val quoteDao = AppDatabase.getDatabase(application).quoteDao()
    private val apiService = WorldWisdomApi.retrofitService
    private val repository = QuoteRepository(quoteDao, apiService)

    // LiveData für das Zitat des Tages (direkt aus dem Repository)
    val quoteOfTheDay: LiveData<Quote> = repository.getQuoteOfTheDay()

    // LiveData für die gespeicherten Zitate (direkt aus der Datenbank)
    val savedQuotes: LiveData<List<Quote>> = repository.getSavedQuotes()

    // LiveData für den ausgewählten Autor
    private val _selectedAuthor = MutableLiveData<Author>()
    val selectedAuthor: LiveData<Author> get() = _selectedAuthor

    // LiveData für gefilterte Zitate
    private val _filteredQuotes = MutableLiveData<List<Quote>>()
    val filteredQuotes: LiveData<List<Quote>> get() = _filteredQuotes

    private val _authorQuote = MutableLiveData<Quote>()
    val authorQuote: LiveData<Quote> get() = _authorQuote

    // LiveData für das ausgewählte Zitat
    private val _selectedQuote = MutableLiveData<Quote>()
    val selectedQuote: LiveData<Quote> get() = _selectedQuote

    // LiveData für alle Zitate
    val quotes: LiveData<List<Quote>>

    init {
        // LiveData von Repository abrufen
        quotes = repository.getAllQuotesFromDatabase()

        // Zitate aktualisieren
        refreshQuotes()
        fetchQuoteOfTheDay()
    }

    // Methode zum Setzen des ausgewählten Autors
    fun selectAuthor(author: Author) {
        _selectedAuthor.value = author
    }

    // Methode zum Setzen des ausgewählten Zitats
    fun selectQuote(quote: Quote) {
        _selectedQuote.value = quote
    }

    // Methode zum Abrufen der Zitate
    fun getQuotes() {
        viewModelScope.launch {
            repository.refreshQuotes()
        }
    }

    // Funktion zum Aktualisieren der Zitate
    fun refreshQuotes() {
        viewModelScope.launch {
            try {
                repository.refreshQuotes() // Zitate aktualisieren
                // Die gefilterten Zitate auf die aktuelle Zitatliste setzen
                _filteredQuotes.value = repository.getAllQuotesFromDatabase().value
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Failed to refresh quotes: ${e.message}")
            }
        }
    }

    // Funktion zum Abrufen des Zitats des Tages
    fun fetchQuoteOfTheDay() {
        viewModelScope.launch {
            repository.refreshQuoteOfTheDay() // Aktualisiere das Zitat des Tages in der Datenbank
        }
    }


    // Filtermethode für AllQuotesFragment (nach Autorname oder Keywords)
    fun filterQuotesForAll(keyword: String) {
        quotes.value?.let { allQuotes ->
            val filteredQuotes = if (keyword.isEmpty()) {
                allQuotes
            } else {
                allQuotes.filter { quote ->
                    quote.keywords.contains(keyword, ignoreCase = true) || quote.author.name.contains(keyword, ignoreCase = true) // Filter nach Keywords oder Autorname
                }
            }
            _filteredQuotes.value = filteredQuotes // Aktualisiere die LiveData
        }
    }


    // Funktion zum Löschen eines Kommentars aus einem Zitat
    fun deleteComment(quote: Quote) {
        // Setze den Kommentar auf null
        val updatedQuote = quote.copy(comments = null)
        viewModelScope.launch {
            repository.updateQuote(updatedQuote) // Aktualisiere das Zitat in der Datenbank
        }
    }

    // Funktion zum Hinzufügen oder Aktualisieren eines Kommentars zu einem Zitat
    fun updateComment(quote: Quote, comment: String) {
        // Aktualisiere den Kommentar im Zitat
        val updatedQuote = quote.copy(comments = comment)

        // Speichere das Zitat mit dem aktualisierten Kommentar in der Datenbank
        viewModelScope.launch {
            repository.updateQuote(updatedQuote)
        }
    }


    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            repository.updateQuote(quote)
        }
    }

    // Methode zum Laden eines Zitats für einen Autor
    fun loadQuoteForAuthor(authorName: String) {
        viewModelScope.launch {
            _authorQuote.postValue(repository.getQuotesByAuthorName(authorName).firstOrNull())
        }
    }

    fun filterQuotesByCategory(category: String): LiveData<List<Quote>> {
        return repository.getQuotesByKeyword(category)
    }

    // Methode zum Umschalten des Speicherstatus eines Zitats
    fun toggleQuoteSavedState(quote: Quote, callback: (String) -> Unit) {
        viewModelScope.launch {
            if (quote.isSaved) {
                // Zitat entfernen (Speicherstatus auf false setzen)
                val updatedQuote = quote.copy(isSaved = false)
                repository.updateQuote(updatedQuote) // Aktualisiere das Zitat in der Datenbank
                callback("Zitat wurde entfernt!") // Callback für Toast-Nachricht
            } else {
                // Zitat speichern (Speicherstatus auf true setzen)
                val updatedQuote = quote.copy(isSaved = true)
                repository.saveQuote(updatedQuote) // Speichere das aktualisierte Zitat über das Repository
                callback("Zitat wurde gespeichert!") // Callback für Toast-Nachricht
            }
        }
    }
}