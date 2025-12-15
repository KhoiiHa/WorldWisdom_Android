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

    private val _userName = MutableLiveData("Guest")
    val userName: LiveData<String> = _userName

    private val _dailyAffirmation = MutableLiveData<Quote?>(null)
    val dailyAffirmation: LiveData<Quote?> = _dailyAffirmation

    private var allQuotesCache: List<Quote> = emptyList()

    private var currentTag: String = "Alle"
    private var currentQuery: String = ""

    private fun applyFilters() {
        val source = allQuotesCache
        if (source.isEmpty()) {
            _quotes.value = emptyList()
            return
        }

        val lowerQuery = currentQuery.trim().lowercase()

        val filtered = source
            .asSequence()
            .filter { quote ->
                // Tag filter
                currentTag.equals("Alle", ignoreCase = true) ||
                        quote.tags.any { t -> t.equals(currentTag, ignoreCase = true) }
            }
            .filter { quote ->
                // Search filter
                lowerQuery.isBlank() ||
                        quote.quote.lowercase().contains(lowerQuery) ||
                        quote.author.lowercase().contains(lowerQuery)
            }
            .toList()

        _quotes.value = filtered
    }

    private fun pickDailyAffirmation() {
        _dailyAffirmation.value = if (allQuotesCache.isNotEmpty()) {
            allQuotesCache.random()
        } else {
            null
        }
    }

    private val _selectedAuthorSlug = MutableLiveData<String?>(null)
    val selectedAuthorSlug: LiveData<String?> = _selectedAuthorSlug

    fun setSelectedAuthorSlug(slug: String?) {
        _selectedAuthorSlug.value = slug
    }

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
                _error.value = null

                // Apply current filters (tag + search) to the fresh data
                applyFilters()

                // Daily affirmation from the full cache
                pickDailyAffirmation()
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
        currentTag = tag
        _error.value = null
        applyFilters()
    }

    fun loadDailyAffirmation() {
        if (allQuotesCache.isEmpty()) {
            _dailyAffirmation.value = null
            Log.w("HomeViewModel", "No quotes available for daily affirmation")
            return
        }

        pickDailyAffirmation()
        _error.value = null
    }

    fun searchQuotes(query: String) {
        currentQuery = query
        _error.value = null
        applyFilters()
    }
}
