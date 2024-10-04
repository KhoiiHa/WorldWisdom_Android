package com.example.projektworldwisdom.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.projektworldwisdom.local.AppDatabase
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuoteRepository

    // LiveData für Zitate direkt vom Repository
    val quotes: LiveData<List<Quote>>

    init {
        // Repository initialisieren
        val quoteDao = AppDatabase.getDatabase(application).quoteDao()
        val apiService = WorldWisdomApi.retrofitService
        repository = QuoteRepository(quoteDao, apiService)

        // LiveData von Repository abrufen
        quotes = repository.getAllQuotes()

        // Zitate und Autoren aktualisieren
        refreshQuotesforLocal()
    }

    // Holt alle Zitate und Autoren
    private fun refreshQuotesforLocal() {
        viewModelScope.launch {
            try {
                repository.refreshQuotes() // Zitate und Autoren aktualisieren
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to refresh quotes: ${e.message}")
            }
        }
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

}