package com.example.projektworldwisdom.home

import androidx.lifecycle.*
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Quote
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>?>()
    val quotes: LiveData<List<Quote>?> = _quotes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _affirmation = MutableLiveData<Quote?>()
    val affirmation: LiveData<Quote?> = _affirmation

    // LiveData für Schlüsselwörter
    private val _keywords = MutableLiveData<List<Keyword>>()
    val keywords: LiveData<List<Keyword>> = _keywords

    init {
        loadQuoteOfTheDay() // Initiales Laden des Zitats des Tages
    }

    fun clearError() {
        _error.postValue(null)
    }

    fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.getQuoteOfTheDay()
                _affirmation.postValue(result) // Direktes Zuweisen des Ergebnisses
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden des Zitats des Tages: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadQuotesByKeyword(keyword: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.getQuotesByKeyword(keyword)
                _quotes.postValue(result)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
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
                _quotes.postValue(allQuotes)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadMultipleRandomQuotes(count: Int) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.getMultipleRandomQuotes(count)
                _quotes.postValue(result)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der zufälligen Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchQuotesByKeyword(keyword: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.searchQuotesByKeyword(keyword)
                _quotes.postValue(result)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler bei der Suche nach Zitaten: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadKeywords() {
        viewModelScope.launch {
            try {
                val result = repository.getKeywords()
                _keywords.postValue(result) // Direktes Zuweisen der Liste von Keyword-Objekten
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Schlüsselwörter: ${e.message}")
            }
        }
    }
}