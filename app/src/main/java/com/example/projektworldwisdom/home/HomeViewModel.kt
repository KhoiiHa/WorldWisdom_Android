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

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _dailyAffirmation = MutableLiveData<Quote>()
    val dailyAffirmation: LiveData<Quote> = _dailyAffirmation

    private var allQuotesCache: List<Quote> = emptyList()

    val _selectedAuthorSlug = MutableLiveData<String?>()
    val selectedAuthorSlug: LiveData<String?> = _selectedAuthorSlug

    fun clearError() {
        _error.value = null
    }

    init {
        loadQuotes() // Lade die Liste der Zitate
    }

    fun loadQuotes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = WorldWisdomApi.retrofitService.getAllQuotes()
                Log.d("HomeViewModel", "Fetched ${result.size} quotes from API")

                allQuotesCache = result
                _quotes.value = result
                _error.value = null

                // Wenn wir Zitate bekommen haben, wähle direkt eine tägliche Affirmation
                if (result.isNotEmpty()) {
                    _dailyAffirmation.value = result.random()
                }
            } catch (e: IOException) {
                _error.value = "Netzwerkfehler: ${e.message}"
                Log.e("HomeViewModel", "Network error fetching quotes", e)
            } catch (e: HttpException) {
                _error.value = "API-Fehler: ${e.code()} - ${e.message()}"
                Log.e("HomeViewModel", "HTTP error fetching quotes", e)
            } catch (e: Exception) {
                _error.value = "Unbekannter Fehler: ${e.message}"
                Log.e("HomeViewModel", "Fehler beim Laden der Zitate", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadQuotesByTag(tag: String) {
        _isLoading.value = true

        val source = allQuotesCache
        if (source.isEmpty()) {
            _isLoading.value = false
            _error.value = "Keine Zitate zum Filtern verfügbar."
            return
        }

        val filtered = if (tag.equals("Alle", ignoreCase = true)) {
            source
        } else {
            source.filter { quote ->
                quote.tags.any { t -> t.equals(tag, ignoreCase = true) }
            }
        }

        _quotes.value = filtered
        _error.value = null
        _isLoading.value = false
    }

    fun loadDailyAffirmation() {
        val source = if (allQuotesCache.isNotEmpty()) {
            allQuotesCache
        } else {
            _quotes.value.orEmpty()
        }

        if (source.isNotEmpty()) {
            _dailyAffirmation.value = source.random()
            _error.value = null
        } else {
            _error.value = "Keine Zitate für die tägliche Affirmation verfügbar."
            Log.w("HomeViewModel", "No quotes available for daily affirmation")
        }
    }

    fun searchQuotes(query: String) {
        _isLoading.value = true

        val source = allQuotesCache
        if (source.isEmpty()) {
            _isLoading.value = false
            _error.value = "Keine Zitate zum Durchsuchen verfügbar."
            return
        }

        val lowerQuery = query.trim().lowercase()
        val filtered = source.filter { quote ->
            quote.quote.lowercase().contains(lowerQuery) ||
                    quote.author.lowercase().contains(lowerQuery)
        }

        _quotes.value = filtered
        _error.value = null
        _isLoading.value = false
    }
}
