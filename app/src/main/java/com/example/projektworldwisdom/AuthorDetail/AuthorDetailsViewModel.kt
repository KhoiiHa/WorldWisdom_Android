package com.example.projektworldwisdom.authordetail


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthorDetailsViewModel(private val repository: QuoteRepository) : ViewModel() {

    // LiveData für Autor-Details
    private val _authorDetails = MutableLiveData<Author?>()
    val authorDetails: LiveData<Author?> = _authorDetails

    // LiveData für das aktuelle Zitat des Autors
    private val _authorQuote = MutableLiveData<Quote?>()
    val authorQuote: LiveData<Quote?> = _authorQuote

    // LiveData für Ladezustände
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isQuoteLoading = MutableLiveData<Boolean>(false)
    val isQuoteLoading: LiveData<Boolean> = _isQuoteLoading

    // LiveData für Fehlernachrichten
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Löscht Fehlernachrichten
    fun clearError() {
        _error.postValue(null)
    }

    // Setzt das initiale Zitat
    fun setInitialQuote(quote: Quote) {
        _authorQuote.postValue(quote)
    }

    // Lädt die Autorendetails anhand des Slugs
    fun loadAuthorDetails(authorSlug: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            Log.d("AuthorDetailsViewModel", "Loading author details for slug: $authorSlug") // Logging des Slugs
            try {
                val author = repository.getAuthorByName(authorSlug)
                if (author != null) {
                    _authorDetails.postValue(author)
                } else {
                    _error.postValue("Autor nicht gefunden")
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Autordetails: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Error fetching author details", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Lädt ein neues Zitat desselben Autors
    fun loadNewQuote() {
        val authorName = _authorDetails.value?.name ?: return
        viewModelScope.launch {
            _isQuoteLoading.postValue(true)
            try {
                val authorQuotes = repository.getQuotesByAuthor(authorName)
                if (authorQuotes.isNotEmpty()) {
                    _authorQuote.postValue(authorQuotes.random())
                } else {
                    _error.postValue("Keine Zitate für diesen Autor gefunden")
                }
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Network error fetching quotes", e)
            } catch (e: HttpException) {
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
                Log.e("AuthorDetailsViewModel", "HTTP error fetching quotes", e)
            } finally {
                _isQuoteLoading.postValue(false)
            }
        }
    }
}