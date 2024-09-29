package com.example.projektworldwisdom.allquotes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.repository.QuoteRepository
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch

class AllQuotesViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _filteredQuotes = MutableLiveData<List<Quote>>(emptyList())
    val filteredQuotes: LiveData<List<Quote>> = _filteredQuotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _searchKeyword = MutableLiveData<String>()
    val searchKeyword: LiveData<String> = _searchKeyword

    private val _selectedKeywords = MutableLiveData<List<String>>(emptyList())
    val selectedKeywords: LiveData<List<String>> = _selectedKeywords

    private val _availableKeywords = MutableLiveData<List<String>>()
    val availableKeywords: LiveData<List<String>> = _availableKeywords

    private val _searchTag = MutableLiveData<String>()
    val searchTag: LiveData<String> = _searchTag

    private val _authors = MutableLiveData<List<Author>>()
    val authors: LiveData<List<Author>> = _authors

    private val _suggestedAuthors = MutableLiveData<List<Author>>()
    val suggestedAuthors: LiveData<List<Author>> = _suggestedAuthors

    init {
        loadAllQuotes()
        loadAvailableKeywords()
        loadAllAuthors()
    }

    fun loadAllQuotes() {
        viewModelScope.launch {
            setLoading(true)
            try {
                val allQuotes = repository.getAllQuotes()
                _quotes.postValue(allQuotes)
                _filteredQuotes.postValue(allQuotes)
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                setLoading(false)
            }
        }
    }

    fun saveQuote(quote: Quote) {
        viewModelScope.launch {
            try {
                repository.saveQuote(quote)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Speichern des Zitats", e)
            }
        }
    }

    private fun loadAvailableKeywords() {
        viewModelScope.launch {
            try {
                val keywords = repository.getAvailableKeywords()
                _availableKeywords.postValue(keywords)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Schlüsselwörter", e)
            }
        }
    }

    private fun loadAllAuthors() {
        viewModelScope.launch {
            try {
                val authorsList = repository.getAllAuthors()
                _authors.postValue(authorsList)
            } catch (e: Exception) {
                Log.e("AllQuotesViewModel", "Fehler beim Laden der Autoren", e)
            }
        }
    }

    fun updateSuggestedAuthors(query: String) {
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                val normalizedQuery = normalizeString(query)
                val suggestions = _authors.value?.filter {
                    it.name.contains(normalizedQuery, ignoreCase = true)
                } ?: emptyList()
                _suggestedAuthors.postValue(suggestions)
            } else {
                _suggestedAuthors.postValue(emptyList())
            }
        }
    }

    private fun updateFilteredQuotes() {
        viewModelScope.launch {
            setLoading(true)
            try {
                // Normalisiere die Eingaben
                val author = normalizeString(_searchKeyword.value)
                val keywords = _selectedKeywords.value ?: emptyList()
                val tag = normalizeString(_searchTag.value)

                // Filtere die Zitate basierend auf den Eingaben
                val filteredQuotes = when {
                    author.isNotEmpty() && keywords.isNotEmpty() && tag.isNotEmpty() ->
                        repository.searchQuotesByAuthorAndKeywordsAndTag(author, keywords, tag)
                    author.isNotEmpty() && keywords.isNotEmpty() ->
                        repository.searchQuotesByAuthorAndKeywordsAndTag(author, keywords, tag)
                    author.isNotEmpty() ->
                        repository.searchQuotesByAuthor(author)
                    keywords.isNotEmpty() ->
                        repository.getQuotesByKeywords(keywords)
                    tag.isNotEmpty() ->
                        repository.getQuotesByTag(tag)
                    else ->
                        repository.getAllQuotes()
                }

                // Aktualisiere die gefilterten Zitate
                _filteredQuotes.postValue(filteredQuotes)
            } catch (e: Exception) {
                handleLoadingError(e)
            } finally {
                setLoading(false)
            }
        }
    }
    fun filterByKeyword(selectedKeywords: List<String>) {
        _selectedKeywords.value = selectedKeywords
        updateFilteredQuotes()
    }

    fun searchByAuthorAndKeywords(authorName: String, keywords: List<String>) {
        _searchKeyword.value = normalizeString(authorName)
        _selectedKeywords.value = keywords.map { normalizeString(it) }
        updateFilteredQuotes()
    }

    private suspend fun handleLoadingError(e: Exception) {
        val localQuotes = repository.getAllQuotesFromLocal()
        if (localQuotes.isNotEmpty()) {
            _quotes.postValue(localQuotes)
            _filteredQuotes.postValue(localQuotes)
        } else {
            _error.postValue("Fehler beim Laden der Zitate: ${e.message}")
            Log.e("AllQuotesViewModel", "Fehler beim Laden der Zitate", e)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    private fun normalizeString(input: String?): String {
        return input?.trim()?.lowercase() ?: ""
    }
}