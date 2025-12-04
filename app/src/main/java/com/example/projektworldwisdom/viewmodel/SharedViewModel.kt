package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

class SharedViewModel : ViewModel() {

    // komplette Zitate-Liste (z.B. aus DB / API – aktuell nur placeholder)
    private val _quotes = MutableLiveData<List<Quote>>(emptyList())
    val quotes: LiveData<List<Quote>> = _quotes

    // aktuell gefilterte Liste
    private val _filteredQuotes = MutableLiveData<List<Quote>>(emptyList())
    val filteredQuotes: LiveData<List<Quote>> = _filteredQuotes

    // Quote of the Day
    private val _quoteOfTheDay = MutableLiveData<Quote?>()
    val quoteOfTheDay: LiveData<Quote?> = _quoteOfTheDay

    // Selektion für das AuthorDetailsFragment
    private val _selectedAuthor = MutableLiveData<Author?>()
    val selectedAuthor: LiveData<Author?> = _selectedAuthor

    private val _selectedQuote = MutableLiveData<Quote?>()
    val selectedQuote: LiveData<Quote?> = _selectedQuote

    // -------------------------------------------------------------------------
    // Daten laden / filtern
    // -------------------------------------------------------------------------

    /**
     * Platzhalter: hier später DB / API anbinden.
     * Aktuell bleibt die Liste leer, damit der Code kompiliert.
     */
    fun getQuotes() {
        // TODO: Repository anbinden und _quotes.value setzen
        // Beispiel:
        // _quotes.value = repository.getAllQuotes()
    }

    /**
     * Filtert die aktuell geladene Liste anhand einer Kategorie.
     * Aktuell noch Platzhalter, damit das Projekt kompiliert.
     */
    fun filterQuotesByCategory(category: String) {
        val allQuotes = _quotes.value.orEmpty()

        // TODO: Hier später echte Filter-Logik einbauen, z.B. über quote.category / tags etc.
        // Temporär: kein echter Filter, nur Rückgabe der kompletten Liste,
        // damit der Code kompiliert und die UI nicht crasht.
        _filteredQuotes.value = allQuotes
    }

    /**
     * Setzt den Filter zurück (zeigt alle Zitate).
     */
    fun clearFilter() {
        _filteredQuotes.value = _quotes.value.orEmpty()
    }

    /**
     * Wählt eine zufällige Quote als "Quote of the Day".
     */
    fun fetchQuoteOfTheDay() {
        val source = _quotes.value.orEmpty()
        _quoteOfTheDay.value = if (source.isNotEmpty()) {
            source.random()
        } else {
            null
        }
    }

    // -------------------------------------------------------------------------
    // Home-Screen Daten (Username + Affirmation des Tages)
    // -------------------------------------------------------------------------

    private val _userName = MutableLiveData<String>("Benutzer")
    val userName: LiveData<String> = _userName

    private val _affirmationText = MutableLiveData<String>("Stay focused and keep learning 💡")
    val affirmationText: LiveData<String> = _affirmationText

    private val _affirmationAuthor = MutableLiveData<String>("ChatGPT Mentor")
    val affirmationAuthor: LiveData<String> = _affirmationAuthor

    /**
     * Setzt Username dynamisch (falls später Login kommt)
     */
    fun setUserName(name: String) {
        _userName.value = name
    }

    /**
     * Setzt Affirmation manuell oder über eine API
     */
    fun updateAffirmation(text: String, author: String) {
        _affirmationText.value = text
        _affirmationAuthor.value = author
    }
    // -------------------------------------------------------------------------
    // Selektion für Details
    // -------------------------------------------------------------------------

    fun selectAuthor(author: Author) {
        _selectedAuthor.value = author
    }

    fun selectQuote(quote: Quote) {
        _selectedQuote.value = quote
    }
}