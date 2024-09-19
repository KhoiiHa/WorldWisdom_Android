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

    private var currentAuthorId: Int? = null // Zum Speichern der authorId

    fun loadAuthorDetails(authorSlug: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                // Versuche zuerst, den Autor im Repository zu finden
                val author = repository.getAuthorBySlug(authorSlug)

                if (author != null) {
                    _authorDetails.postValue(author)
                    loadRandomQuoteByAuthor(authorSlug) // Lade ein Zitat, wenn der Autor gefunden wurde
                } else {
                    // Wenn der Autor nicht im Repository gefunden wurde, rufe ihn von der API ab
                    val allAuthors: List<Author> = WorldWisdomApi.retrofitService.getAuthors()
                    val apiAuthor = allAuthors.firstOrNull { it.tag == authorSlug }

                    if (apiAuthor != null) {
                        _authorDetails.postValue(apiAuthor)
                        repository.addAuthor(apiAuthor) // Füge den Autor zum Repository hinzu
                        loadRandomQuoteByAuthor(authorSlug)
                    } else {
                        _error.postValue("Autor nicht gefunden")
                    }
                }
            } catch (e: Exception) {
                _error.postValue("Fehler beim Laden der Autordetails: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Error fetching author details", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadRandomQuoteByAuthor(authorSlug: String) {
        viewModelScope.launch {
            _isQuoteLoading.postValue(true)
            _isLoading.postValue(true)
            try {
                // Versuche, Zitate des Autors aus dem Repository abzurufen (sowohl lokal als auch von der API)
                // Wir verwenden getQuotesByKeyword, da es sowohl lokal als auch in der API sucht und die authorId berücksichtigt
                val authorQuotes = repository.getQuotesByKeyword(authorSlug)

                if (authorQuotes.isNotEmpty()) {
                    _authorQuote.postValue(authorQuotes.random())
                } else {
                    _error.postValue("Keine Zitate für diesen Autor gefunden")
                }
            } catch (e: IOException) {
                _error.postValue("Netzwerkfehler: ${e.message}")
                Log.e("AuthorDetailsViewModel", "Network error fetching quotes", e)
            } catch (e: HttpException) {
                // Handle spezifischere HTTP-Fehler, falls nötig (z.B. 404 für "Autor nicht gefunden")
                _error.postValue("API-Fehler: ${e.code()} - ${e.message()}")
                Log.e("AuthorDetailsViewModel", "HTTP error fetching quotes", e)
            } finally {
                _isQuoteLoading.postValue(false)
                _isLoading.postValue(false)
            }
        }
    }
}