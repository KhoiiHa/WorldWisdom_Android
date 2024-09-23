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

    private val _authorDetails = MutableLiveData<Author?>()
    val authorDetails: LiveData<Author?> = _authorDetails

    private val _authorQuote = MutableLiveData<Quote?>()
    val authorQuote: LiveData<Quote?> = _authorQuote

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isQuoteLoading = MutableLiveData<Boolean>(false)
    val isQuoteLoading: LiveData<Boolean> = _isQuoteLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun clearError() {
        _error.postValue(null)
    }



    fun loadAuthorDetails(authorSlug: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // Versuche, die Liste aller Autoren vom Repository abzurufen
                val allAuthors: List<Author> = repository.getAllAuthors()
                val apiAuthor = allAuthors.firstOrNull { it.tag == authorSlug }

                if (apiAuthor != null) {
                    _authorDetails.postValue(apiAuthor)
                    loadRandomQuoteByAuthor(apiAuthor.name) // Lade ein Zitat, wenn der Autor gefunden wurde
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

    fun loadRandomQuoteByAuthor(authorName: String) {
        viewModelScope.launch {
            _isQuoteLoading.postValue(true)
            try {
                // Versuche, die Zitate des Autors von der API abzurufen
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