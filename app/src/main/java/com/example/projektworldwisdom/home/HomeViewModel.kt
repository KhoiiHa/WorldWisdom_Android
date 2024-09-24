package com.example.projektworldwisdom.home

import android.util.Log
import androidx.lifecycle.*
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.repository.QuoteRepository
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

    fun clearError() {
        _error.value = null
    }

    init {
        loadQuoteOfTheDay() // Initiales Laden des Zitats des Tages
        loadAllQuotesHome() // Initiales Laden aller Zitate
    }

    fun loadQuoteOfTheDay() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError() // Fehlerstatus zurücksetzen

            try {
                val quote = repository.getQuoteOfTheDay() ?: repository.getQuoteOfTheDayFromLocal()
                _dailyAffirmation.postValue(quote)
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden des Zitats: ${e.message}")
                _dailyAffirmation.postValue(null)
            } finally {
                _isLoading.postValue(false) // Ladezustand zurücksetzen
            }
        }
    }

    fun loadAllQuotesHome() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val allQuotes = repository.getAllQuotes()
                Log.d("HomeViewModel", "Fetched quotes: $allQuotes")

                if (allQuotes.isEmpty()) {
                    _error.postValue("Keine Zitate gefunden.")
                } else {
                    _quotes.postValue(allQuotes)
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun handleQuotesResponse(quotes: List<Quote>, keyword: String) {
        if (quotes.isEmpty()) {
            _error.postValue("Keine Zitate für das Schlüsselwort '$keyword' gefunden.")
        } else {
            _quotes.postValue(quotes)
            _error.postValue(null) // Fehlermeldung zurücksetzen
        }
    }

    fun loadQuotesByKeywords(keywords: List<String>) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // Abrufen der Zitate von der Repository unter Verwendung der Liste von Keywords
                val quotes = repository.getQuotesByKeywords(keywords)
                // Übergabe der Zitate und der Keywords an die Funktion
                handleQuotesResponse(quotes, keywords.joinToString(", ")) // Verwendung der Keywords als String
            } catch (e: Exception) {
                _error.postValue("Fehler beim Abrufen der Zitate: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByAuthor(authorName: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            if (authorName.isBlank()) {
                _error.postValue("Bitte geben Sie einen gültigen Autorennamen ein.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                val quotes = repository.searchQuotesByAuthor(authorName)
                if (quotes.isEmpty()) {
                    val localQuotes = repository.getQuotesByAuthor(authorName)
                    handleQuotesResponse(localQuotes, authorName)
                } else {
                    _quotes.postValue(quotes)
                    _error.postValue(null) // Fehlermeldung zurücksetzen
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für den Autor: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchByKeyword(keyword: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            clearError()

            if (keyword.isBlank()) {
                _error.postValue("Bitte geben Sie ein gültiges Schlüsselwort ein.")
                _isLoading.postValue(false)
                return@launch
            }

            try {
                // Hier wird das Keyword in eine Liste umgewandelt, um es der Funktion zu übergeben
                val quotes = repository.getQuotesByKeywords(listOf(keyword))
                handleQuotesResponse(quotes, keyword) // Verwende nur die Zitate, ohne das Keyword zu übergeben
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Zitate für das Schlüsselwort: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getAuthorByName(authorName: String): LiveData<String?> {
        val authorNameLiveData = MutableLiveData<String?>()

        if (authorName.isBlank()) {
            authorNameLiveData.postValue(null)
            return authorNameLiveData
        }

        viewModelScope.launch {
            try {
                val author = repository.getAuthorByName(authorName)
                authorNameLiveData.postValue(author?.name)
            } catch (e: Exception) {
                authorNameLiveData.postValue(null)
                Log.e("HomeViewModel", "Fehler beim Abrufen des Autors: ${e.message}")
            }
        }
        return authorNameLiveData
    }
}