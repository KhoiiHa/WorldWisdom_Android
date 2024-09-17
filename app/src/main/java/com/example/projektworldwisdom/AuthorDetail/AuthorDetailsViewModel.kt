package com.example.projektworldwisdom.authordetail


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AuthorDetailsViewModel : ViewModel() {

    private val _authorDetails = MutableLiveData<Author?>()
    val authorDetails: LiveData<Author?> = _authorDetails

    private val _authorQuote = MutableLiveData<Quote?>()
    val authorQuote: LiveData<Quote?> = _authorQuote

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun clearError() {
        _error.postValue(null)
    }

    fun loadAuthorDetails(authorSlug: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // Abrufen aller Autoren von der API
                val allAuthors: List<Author> = WorldWisdomApi.retrofitService.getAuthors()
                // Finden des Autors basierend auf dem Slug
                val author = allAuthors.firstOrNull { it.tag == authorSlug }
                _authorDetails.postValue(author)
                if (author != null) {
                    _error.postValue(null)
                    loadRandomQuoteByAuthor(authorSlug) // Lade ein Zitat, wenn der Autor gefunden wurde
                } else {
                    _error.postValue("Autor nicht gefunden")
                }
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Network error fetching author details", e)
            } catch (e: HttpException) {
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
                Log.e("AuthorDetailsViewModel", "HTTP error fetching author details", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadRandomQuoteByAuthor(authorSlug: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val result = WorldWisdomApi.retrofitService.searchQuotesByKeyword(authorSlug)
                _authorQuote.postValue(result.randomOrNull()) // Zufälliges Zitat aus der Liste auswählen
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Network error fetching quotes", e)
            } catch (e: HttpException) {
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
                Log.e("AuthorDetailsViewModel", "HTTP error fetching quotes", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}