package com.example.projektworldwisdom.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

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

    private val _dailyAffirmation = MutableLiveData<Quote>()
    val dailyAffirmation: LiveData<Quote> = _dailyAffirmation

    val _selectedAuthorSlug = MutableLiveData<String?>()
    val selectedAuthorSlug: LiveData<String?> = _selectedAuthorSlug

    // Moshi-Adapter einmalig erstellen
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val quoteAdapter = moshi.adapter(Quote::class.java)

    private lateinit var application: Application

    private lateinit var sharedPrefs: SharedPreferences

    fun setSharedPrefs(sharedPrefs: SharedPreferences) {
        this.sharedPrefs = sharedPrefs
    }

//    private val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

//    private fun getSharedPrefs(): SharedPreferences {
//        return application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//    }


    init {
        loadQuotes() // Lade die Liste der Zitate
    }



    //Überprüft, ob das letzte Aktualisierungsdatum mit dem aktuellen Datum übereinstimmt. Wenn nicht, ist eine Aktualisierung erforderlich.
//    private fun isQuoteUpdateNeeded(): Boolean {
//        val lastUpdateDate = getSharedPrefs().getString("last_update_date", null)
//        val currentDate = LocalDate.now().toString()
//        return lastUpdateDate != currentDate
//    }

//    fun initializeData() {
//        // Überprüfe, ob heute bereits ein Zitat geladen wurde
//        if (isQuoteUpdateNeeded()) {
//            loadDailyAffirmation()
//        } else {
//            // Lade das letzte Zitat aus SharedPreferences (falls vorhanden)
//            val savedQuoteJson = getSharedPrefs().getString("daily_affirmation", null)
//            savedQuoteJson?.let {
//                _dailyAffirmation.value = quoteAdapter.fromJson(it)
//            }
//        }
//    }

    fun loadQuotes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.getMultipleRandomQuotes(count = 20)
                _quotes.postValue(result.results)
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
                _quotes.postValue(result.results)
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

    fun loadDailyAffirmation() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.getRandomQuote()
                _dailyAffirmation.postValue(result)
                _error.postValue(null)
            } catch (e: Exception) {
                // Allgemeine Fehlerbehandlung
                Log.e("HomeViewModel", "Fehler beim Laden der Zitate", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchQuotes(query: String) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Ladeanzeige starten
            try {
                val result = WorldWisdomApi.retrofitService.searchQuotes(query)
                _quotes.postValue(result.results) // Suchergebnisse im LiveData speichern
            } catch (e: Exception) {
                // Fehlerbehandlung
                _error.postValue("Fehler bei der Suche: ${e.message}")
                Log.e("HomeViewModel", "Error searching quotes", e)
            } finally {
                _isLoading.postValue(false) // Ladeanzeige beenden
            }
        }
    }
}
