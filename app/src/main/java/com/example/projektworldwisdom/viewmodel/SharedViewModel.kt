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

    private val repository: QuoteRepository

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

    private val _savedQuotes = MutableLiveData<List<Quote>>()
    val savedQuotes: LiveData<List<Quote>> get() = _savedQuotes

    // LiveData für alle Zitate
    val quotes: LiveData<List<Quote>>

    init {
        // Repository initialisieren
        val quoteDao = AppDatabase.getDatabase(application).quoteDao()
        val apiService = WorldWisdomApi.retrofitService
        repository = QuoteRepository(quoteDao, apiService)

        // LiveData von Repository abrufen
        quotes = repository.getAllQuotesFromDatabase()

        // Zitate aktualisieren
        refreshQuotes()
    }

    /// Methode zum Setzen des ausgewählten Autors
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
            // Ruft die aktuellen Zitate von der API ab und speichert sie in der Datenbank
            repository.refreshQuotes()
        }
    }

    // Speichert ein Zitat
    fun saveQuote(quote: Quote) {
        viewModelScope.launch {
            // Hier wird das Zitat in die Datenbank eingefügt
            repository.saveQuote(quote)
        }
    }

    // Funktion zum Aktualisieren der Zitate
    fun refreshQuotes() {
        viewModelScope.launch {
            try {
                repository.refreshQuotes() // Zitate aktualisieren
                // Die gefilterten Zitate auf die aktuelle Zitatliste setzen
                _filteredQuotes.value = repository.getAllQuotesFromDatabase().value // Hier holen wir uns die LiveData-Liste
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Failed to refresh quotes: ${e.message}")
            }
        }
    }

    // Filtermethode für HomeFragment (nach Autorname)
    fun filterQuotesForHome(keyword: String) {
        quotes.value?.let { allQuotes ->
            val filteredQuotes = if (keyword.isEmpty()) {
                allQuotes
            } else {
                allQuotes.filter { quote ->
                    quote.author.name.contains(keyword, ignoreCase = true) // Filter nach Autorname
                }
            }
            _filteredQuotes.value = filteredQuotes // Aktualisiere die LiveData
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

    // Methode zum Abrufen der gespeicherten Zitate
    fun getSavedQuotes() {
        viewModelScope.launch {
            // Beobachte die LiveData vom Repository und aktualisiere _savedQuotes
            repository.getSavedQuotes().observeForever { savedQuotes ->
                _savedQuotes.value = savedQuotes
            }
        }
    }

    // Methode zum Löschen eines Zitats
    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.deleteQuoteById(quote.id)
                // Aktualisiere die gespeicherten Zitate nach dem Löschen
                getSavedQuotes()
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Failed to delete quote: ${e.message}")
            }
        }
    }

    fun loadQuoteForAuthor(authorName: String) {
        viewModelScope.launch {
            // Holen Sie sich das erste Zitat für den Autor oder null, wenn kein Zitat gefunden wurde
            _authorQuote.postValue(repository.getQuotesByAuthorName(authorName).firstOrNull())
        }
    }


}