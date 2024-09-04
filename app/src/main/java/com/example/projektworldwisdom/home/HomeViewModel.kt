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

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadQuotes()
    }

    fun loadQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.getMultipleRandomQuotes()
                _quotes.postValue(result.results)
                _error.postValue(null)
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

    fun loadQuotesByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.searchQuotes(tag)
                _quotes.postValue(result.results)
                _error.postValue(null)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    _error.postValue("Keine Zitate für den Tag '$tag' gefunden.")
                } else {
                    _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
                }
                Log.e("HomeViewModel", "Error loading quotes by tag", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}