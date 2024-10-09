package com.example.projektworldwisdom.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    // LiveData für das Zitat des Tages
    private val _quoteOfTheDay = MutableLiveData<Quote>()
    val quoteOfTheDay: LiveData<Quote> get() = _quoteOfTheDay

    // MutableLiveData für die gespeicherten Zitate
    private val _savedQuotes = MutableLiveData<List<Quote>>()
    val savedQuotes: LiveData<List<Quote>> get() = _savedQuotes

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
        refreshQuoteOfTheDay()
        loadSavedQuotes()
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
                _filteredQuotes.value = repository.getAllQuotesFromDatabase().value // Holen wir uns die LiveData-Liste
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Failed to refresh quotes: ${e.message}")
            }
        }
    }

    fun refreshQuoteOfTheDay() {
        viewModelScope.launch {
            // Aktualisiere das Zitat des Tages von der API und speichere es in der Datenbank
            repository.refreshQuoteOfTheDay()

            // Beobachte das Zitat des Tages aus der Datenbank
            repository.getQuoteOfTheDay().observeForever { quote ->
                // Aktualisiere das LiveData-Objekt mit dem neuen Zitat
                _quoteOfTheDay.value = quote
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
            loadSavedQuotes() // Lade die gespeicherten Zitate erneut
        }
    }

    // Funktion zum Hinzufügen oder Aktualisieren eines Kommentars zu einem Zitat
    fun updateComment(quote: Quote, comment: String) {
        // Aktualisiere den Kommentar im Zitat
        val updatedQuote = quote.copy(comments = comment)

        // Speichere das Zitat mit dem aktualisierten Kommentar in der Datenbank
        viewModelScope.launch {
            repository.updateQuote(updatedQuote)
            loadSavedQuotes() // Aktualisiere die Liste der gespeicherten Zitate
        }
    }


    // Methode zum Speichern eines Zitats
    fun saveQuote(quote: Quote) {
        // Setze isSaved auf true, bevor du das Zitat speicherst
        val updatedQuote = quote.copy(isSaved = true)
        viewModelScope.launch {
            repository.saveQuote(updatedQuote) // Speichere das aktualisierte Zitat über das Repository
        }
    }

    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            repository.updateQuote(quote)
        }
    }

    // Funktion zum Laden der gespeicherten Zitate
    fun loadSavedQuotes() {
        viewModelScope.launch {
            // Wir beobachten die LiveData von Repository und setzen die _savedQuotes entsprechend
            repository.getSavedQuotes().observeForever {
                _savedQuotes.value = it
            }
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
            loadSavedQuotes() // Lade die gespeicherten Zitate erneut, um die Änderungen zu reflektieren
        }
    }
}