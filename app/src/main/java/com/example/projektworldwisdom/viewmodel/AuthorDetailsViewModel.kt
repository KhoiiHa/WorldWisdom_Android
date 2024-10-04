package com.example.projektworldwisdom.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class AuthorDetailsViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _authorQuote = MutableLiveData<Quote?>()
    val authorQuote: MutableLiveData<Quote?> get() = _authorQuote


    // Funktion, um ein zufälliges Zitat für den Autor zu laden
    fun loadQuoteForAuthor(authorName: String) {
        viewModelScope.launch {
            // Ruft ein zufälliges Zitat für den angegebenen Autor ab
            _authorQuote.value = repository.getRandomQuoteByAuthor(authorName)
        }
    }


    // Methode zum Laden eines neuen Zitats
    fun loadNewQuote(authorName: String) {
        // Logik, um ein neues Zitat vom selben Autor zu laden
        viewModelScope.launch {
            val newQuote = repository.getRandomQuoteByAuthor(authorName)
            _authorQuote.value = newQuote
        }
    }


}