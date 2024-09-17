package com.example.projektworldwisdom.home

import androidx.lifecycle.*
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Keyword
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
                _dailyAffirmation.postValue(result)
                _error.postValue(null) // Fehlernachricht zurücksetzen
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
            } catch (e: HttpException) {
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
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
                _error.postValue(null) // Fehlernachricht zurücksetzen
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
                _error.postValue(null) // Fehlernachricht zurücksetzen
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
                _error.postValue(null) // Fehlernachricht zurücksetzen
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der zufälligen Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadKeywords() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = repository.getKeywords()
                _keywords.postValue(result)
                _error.postValue(null) // Fehlernachricht zurücksetzen
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Schlüsselwörter: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}