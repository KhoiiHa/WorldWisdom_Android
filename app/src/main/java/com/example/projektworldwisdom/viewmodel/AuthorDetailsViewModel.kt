package com.example.projektworldwisdom.viewmodel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.repository.QuoteRepository
import kotlinx.coroutines.launch

class AuthorDetailsViewModel(private val repository: QuoteRepository) : ViewModel() {

    private val _authorQuote = MutableLiveData<Quote?>()
    val authorQuote: LiveData<Quote?> get() = _authorQuote

    private val _pickedAuthor = MutableLiveData<Author?>()
    val pickedAuthor: LiveData<Author?> get() = _pickedAuthor

    private val _authorQuotes = MutableLiveData<List<Quote>>() // Hier wird die Liste der Zitate hinzugefügt
    val authorQuotes: LiveData<List<Quote>> get() = _authorQuotes // Getter für die Zitate

    // Funktion, um die Details eines Autors und ein zufälliges Zitat für den Autor zu laden
    fun loadAuthorDetails(authorName: String) {
        viewModelScope.launch {
            // Holen der Autorinformationen anhand des Namens
            _pickedAuthor.value = repository.getAuthorByName(authorName) // Korrekte Verwendung des Namens

            // Holen eines zufälligen Zitats für den Autor
            _authorQuote.value = _pickedAuthor.value?.let {
                repository.getRandomQuoteByAuthor(authorName)
            }

            // Holen aller Zitate des Autors
            _pickedAuthor.value?.let {
                _authorQuotes.value = repository.getQuotesByAuthorName(it.name) // Hier wird die Funktion korrekt aufgerufen
            }
        }
    }

    // Methode zum Laden eines neuen Zitats
    fun loadNewQuote(authorName: String) {
        viewModelScope.launch {
            val author = repository.getAuthorByName(authorName)
            if (author != null) {
                _authorQuote.value = repository.getRandomQuoteByAuthor(authorName)
            }
        }
    }
}