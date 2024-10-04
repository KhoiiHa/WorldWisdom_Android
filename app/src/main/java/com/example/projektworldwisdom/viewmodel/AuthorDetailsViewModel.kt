package com.example.projektworldwisdom.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.local.AppDatabase
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class AuthorDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(AppDatabase.getDatabase(application).quoteDao(), WorldWisdomApi.retrofitService)

    private val _authorQuote = MutableLiveData<Quote?>()
    val authorQuote: MutableLiveData<Quote?> get() = _authorQuote

    val pickedAuthor = repository.selectedAuthor


    // Funktion, um ein zufälliges Zitat für den Autor zu laden
    fun loadQuoteForAuthor(authorId: Int) {
        viewModelScope.launch {
            // Ruft ein zufälliges Zitat für den angegebenen Autor ab
            _authorQuote.value = repository.getRandomQuoteByAuthor(authorId)
        }
    }

    fun pickedAuthor(authorId: Int){
        viewModelScope.launch {
            repository.getAuthorById(authorId)
        }
    }


    // Methode zum Laden eines neuen Zitats
    fun loadNewQuote(authorId: Int) {
        // Logik, um ein neues Zitat vom selben Autor zu laden
        viewModelScope.launch {
            val newQuote = repository.getRandomQuoteByAuthor(authorId)
            _authorQuote.value = newQuote
        }
    }


}