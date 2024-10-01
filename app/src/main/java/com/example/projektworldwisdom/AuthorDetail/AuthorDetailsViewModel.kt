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

    // LiveData für Fehlernachrichten
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Löscht Fehlernachrichten
    fun clearError() {
        _error.value = null
    }
    // Funktion, um den Autor anhand des Namens abzurufen
    fun getAuthorByName(authorName: String) {
        viewModelScope.launch {
            val author = repository.getAuthorByName(authorName)
            _authorDetails.postValue(author) // LiveData aktualisieren
        }
    }

    // Setzt das initiale Zitat
    fun setInitialQuote(quote: Quote) {
        _authorQuote.value = quote
    }

    // Lädt die Autorendetails anhand des Slugs
    fun loadAuthorDetails(authorSlug: String) {
        viewModelScope.launch {
            Log.d("AuthorDetailsViewModel", "Versuche, Autorendetails für Slug: $authorSlug zu laden")
            try {
                val author = repository.getAuthorByName(authorSlug)
                Log.d("AuthorDetailsViewModel", "Gefundener Autor: $author") // Logge den gefundenen Autor
                if (author != null) {
                    _authorDetails.postValue(author) // Setze die Autor-Details
                } else {
                    Log.d("AuthorDetailsViewModel", "Kein Autor gefunden für den Slug: $authorSlug")
                }
            } catch (e: Exception) {
                Log.e("AuthorDetailsViewModel", "Fehler beim Laden der Autordetails: ${e.message}") // Fehler loggen
                // Hier kannst du eventuell einen Toast oder eine Snackbar in der UI zeigen, falls du das möchtest
            }
        }
    }

    // Lädt ein neues Zitat desselben Autors

    fun loadNewQuote() {
        val authorName = _authorDetails.value?.name ?: return
        viewModelScope.launch {
            val authorQuotes = repository.getQuotesByAuthor(authorName)
            if (authorQuotes.isNotEmpty()) {
                _authorQuote.value = authorQuotes.random()
            } else {
                _error.value = "Keine Zitate für diesen Autor gefunden"
            }
        }
    }
}