package com.example.projektworldwisdom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.FirebaseRepository
import com.example.projektworldwisdom.remote.QuoteRepository
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CollectionViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()
    private val quoteRepository = QuoteRepository(WorldWisdomApi.retrofitService)

    val downloading: LiveData<Boolean> = firebaseRepository.downloading

    // Eigene Zitat-Liste für die Collection-Ansicht
    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    init {
        getQuotes()
    }


    fun getQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val idCollection = firebaseRepository.getUserCollection()
                val quoteDeferreds = idCollection?.map {
                    // Aktuell gibt es keinen echten ID-basierten Endpunkt.
                    // Für jedes Collection-Item holen wir ein zufälliges Zitat.
                    async { quoteRepository.getRandomQuote() }
                }
                val quotesTMP = quoteDeferreds?.awaitAll()?.filterNotNull() ?: emptyList()

                _quotes.postValue(quotesTMP)
                _error.postValue(null)
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
                Log.e("CollectionViewModel", "Network error fetching quotes", e)
            } catch (e: HttpException) {
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
                Log.e("CollectionViewModel", "HTTP error fetching quotes", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun refreshUserCollection() {
        // Nutzt dieselbe Lade-Logik wie beim Initial-Load
        getQuotes()
    }


}