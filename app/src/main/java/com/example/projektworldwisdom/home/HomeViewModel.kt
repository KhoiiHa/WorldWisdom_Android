package com.example.projektworldwisdom.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.local.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch


class HomeViewModel(private val repository: QuoteRepository) : ViewModel() {

    val _quotes = MutableLiveData<List<Quote>?>()
    val quotes: LiveData<List<Quote>?> = _quotes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> = _userName

    private val _affirmationText = MutableLiveData<String?>()
    val affirmationText: LiveData<String?> = _affirmationText

    private val _affirmationAuthor = MutableLiveData<String?>()
    val affirmationAuthor: LiveData<String?> = _affirmationAuthor

    private val _affirmation = MutableLiveData<Quote?>()
    val affirmation: LiveData<Quote?> = _affirmation


    init {
        loadQuotes()
    }

    fun loadQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // API-Anfrage
                val result = WorldWisdomApi.retrofitService.getRandomQuote()
                _affirmation.postValue(result)
                _error.postValue(null)
                // Speichere das Ergebnis in der lokalen Datenbank
                repository.insertQuote(result)
            } catch (e: Exception) {
                // Fallback: Lade Zitate aus der lokalen Datenbank
                val localQuote = repository.getRandomLocalQuote()
                if (localQuote != null) {
                    _affirmation.postValue(localQuote)
                } else {
                    _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
                    Log.e("HomeViewModel", "Fehler beim Laden der Zitate", e)
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadQuotesByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.searchQuotes(tag)
                _quotes.postValue(result.results)
                // Speichere die abgerufenen Zitate in der lokalen Datenbank
                repository.insertQuotes(result.results)
            } catch (e: Exception) {
                // Fallback: Lade Zitate nach Tag aus der lokalen Datenbank
                val localQuotes = repository.getQuotesByTag(tag)
                if (!localQuotes.isNullOrEmpty()) {
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

    fun loadAllQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val allQuotes = WorldWisdomApi.retrofitService.getAllQuotes()
                _quotes.postValue(allQuotes)
                // Speichere die Zitate in der lokalen Datenbank
                repository.insertQuotes(allQuotes)
            } catch (e: Exception) {
                // Fallback: Lade alle Zitate aus der lokalen Datenbank
                val localQuotes = repository.getAllLocalQuotes()
                if (!localQuotes.isNullOrEmpty()) {
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

    //Funktion im HomeFragment noch einbauen
    fun searchQuotesByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // Suche Zitate nach Tag über die API
                val quotesByTag = repository.searchQuotesByTag(tag)
                _quotes.postValue(quotesByTag)
                _error.postValue(null)
            } catch (e: Exception) {
                // Fallback: Suche Zitate nach Tag in der lokalen Datenbank
                val localQuotes = repository.getQuotesByTagFromLocal(tag)
                if (localQuotes.isNotEmpty()) {
                    _quotes.postValue(localQuotes)
                } else {
                    _error.postValue("Fehler beim Suchen der Zitate: ${e.message}")
                    Log.e("HomeViewModel", "Fehler beim Suchen der Zitate", e)
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    //Funktion im HomeFragment noch einbauen
    fun loadRandomQuote() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val randomQuote = repository.getRandomQuote()
                if (randomQuote != null) {
                    _affirmation.postValue(randomQuote)
                } else {
                    _error.postValue("Kein Zitat gefunden")
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden eines zufälligen Zitats: ${e.message}")
                Log.e("HomeViewModel", "Fehler beim Laden eines zufälligen Zitats", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

}