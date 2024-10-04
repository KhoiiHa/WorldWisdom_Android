package com.example.projektworldwisdom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.local.AppDatabase
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(AppDatabase.getDatabase(application).quoteDao(), WorldWisdomApi.retrofitService)

    // LiveData für die gespeicherten Zitate
    private val _savedQuotes = MutableLiveData<List<Quote>>()
    val savedQuotes: LiveData<List<Quote>> get() = _savedQuotes

    // Gespeicherte Zitate abrufen
    fun getSavedQuotes() {
        viewModelScope.launch {
            _savedQuotes.value = repository.getSavedQuotes().value
        }
    }

    // Ein Zitat löschen
    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            repository.deleteQuoteById(quote.id)
            // Nach dem Löschen die gespeicherten Zitate erneut abrufen
            getSavedQuotes()
        }
    }
}