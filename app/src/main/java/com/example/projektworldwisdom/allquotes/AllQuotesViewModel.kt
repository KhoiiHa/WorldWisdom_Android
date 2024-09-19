package com.example.projektworldwisdom.allquotes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch

class AllQuotesViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchKeyword = MutableLiveData<String>()
    val searchKeyword: LiveData<String> get() = _searchKeyword

    init {
        // Initial alle Zitate laden
        loadAllQuotes()
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val allQuotes = WorldWisdomApi.retrofitService.getAllQuotes()
                _quotes.postValue(allQuotes)
                repository.insertQuotes(allQuotes)
            } catch (e: Exception) {
                val localQuotes = repository.getAllQuotesFromLocal()
                if (localQuotes.isNotEmpty()) {
                    _quotes.postValue(localQuotes)
                } else {
                    _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
                    Log.e("HomeViewModel", "Fehler beim Laden der Zitate", e)
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun filterByKeyword(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true // Ladeanzeige starten

            try {
                val filteredQuotes = if (keyword == "All") {
                    repository.getAllQuotes() // Alle Zitate laden (API zuerst, dann lokal)
                } else {
                    repository.getQuotesByKeyword(keyword) // Gefilterte Zitate laden (API zuerst, dann lokal)
                }
                _quotes.value = filteredQuotes
            } catch (e: Exception) {
                _error.value = "Fehler beim Filtern der Zitate: ${e.message}"
                Log.e("AllQuotesViewModel", "Fehler beim Filtern der Zitate", e)
            } finally {
                _isLoading.value = false // Ladeanzeige stoppen
            }
        }
    }

    fun searchByAuthor(author: String) {
        _searchKeyword.value = author
        viewModelScope.launch {
            _isLoading.postValue(true)

            val searchResults = repository.searchQuotesByAuthor(author)
            _quotes.postValue(searchResults)

            _isLoading.postValue(false)
        }
    }
}