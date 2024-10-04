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


class AllQuotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuoteRepository

    // LiveData für Zitate
    val quotes: LiveData<List<Quote>>

    // LiveData für gefilterte Zitate
    private val _filteredQuotes = MutableLiveData<List<Quote>>()
    val filteredQuotes: LiveData<List<Quote>> get() = _filteredQuotes

    init {
        val quoteDao = AppDatabase.getDatabase(application).quoteDao()
        val apiService = WorldWisdomApi.retrofitService
        repository = QuoteRepository(quoteDao, apiService)

        // Zitate abrufen
        quotes = repository.getAllQuotes()
    }

    // Methode zur Filterung der Zitate
    fun searchQuotes(query: String) {
        viewModelScope.launch {
            // Holen Sie sich alle Zitate aus dem Repository
            val allQuotes = quotes.value ?: emptyList() // Verwende die bereits vorhandene LiveData

            // Filtern der Zitate basierend auf dem Autorennamen oder den Schlüsselwörtern
//            val filtered = allQuotes.filter { quote ->
//                quote.author.any { author -> author.name.contains(query, ignoreCase = true) } ||
//                        quote.keywords.contains(query, ignoreCase = true)
//            }

//            _filteredQuotes.value = filtered // Aktualisiere die gefilterten Zitate
        }
    }

    fun getQuotes() {
        viewModelScope.launch {
            repository.refreshQuotes()
        }
    }

    fun saveQuote(quote: Quote) {
        viewModelScope.launch {
            repository.saveQuote(quote)
        }
    }
}