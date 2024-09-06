package com.example.projektworldwisdom.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException


class HomeViewModel : ViewModel() {

    val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _affirmationText = MutableLiveData<String>()
    val affirmationText: LiveData<String> = _affirmationText

    private val _affirmationAuthor = MutableLiveData<String>()
    val affirmationAuthor: LiveData<String> = _affirmationAuthor

    private val _affirmation = MutableLiveData<Quote>() // Für das Zitat in der CardView
    val affirmation: LiveData<Quote> = _affirmation

    init {
        loadQuotes()
    }

    fun loadQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.getRandomQuote()
                _affirmation.postValue(result) // Aktualisiere das LiveData für die CardView
                _error.postValue(null)
            } catch (e: Exception) {
                // Allgemeine Fehlerbehandlung
                Log.e("HomeViewModel", "Fehler beim Laden der Zitate", e)
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
                _quotes.postValue(result.results) // Aktualisiere das LiveData für das RecyclerView
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
                Log.e("HomeViewModel", "Network error fetching quotes", e)
            } catch (e: HttpException) {
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
                Log.e("HomeViewModel", "HTTP error fetching quotes", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}