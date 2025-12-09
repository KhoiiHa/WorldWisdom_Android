package com.example.projektworldwisdom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.QuoteRepository
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import kotlinx.coroutines.launch

class SharedViewModel(
    private val quoteApi: WorldWisdomApiService = WorldWisdomApi.retrofitService,
    private val quoteRepository: QuoteRepository = QuoteRepository(quoteApi)
) : ViewModel() {

    // ------------------------------------------------------------------------
    // ZENTRALE QUOTE-DATEN (für HomeFragment + CollectionFragment)
    // ------------------------------------------------------------------------

    private val _quotes = MutableLiveData<List<Quote>>()
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // Beim Start direkt Zitate laden
        loadQuotes()
    }

    /**
     * Lädt alle Zitate aus dem Repository.
     */
    fun loadQuotes() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = quoteRepository.getAllQuotes()
                _quotes.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Fehler beim Laden der Zitate."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Wird z.B. vom Fragment aufgerufen, nachdem der Fehler angezeigt wurde.
     * So verschwindet der Fehler wieder aus dem State.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Gibt z. B. das erste Zitat als "Daily Affirmation" zurück.
     * Später kannst du hier z. B. auch ein zufälliges Zitat auswählen.
     */
    fun getDailyQuote(): Quote? {
        val current = _quotes.value.orEmpty()
        return current.firstOrNull()
    }

    /**
     * Hilfsfunktion für die Collection-Ansicht:
     * Filtert die Liste nach Kategorie.
     * category == null oder "All" → komplette Liste.
     */
    fun getQuotesByCategory(category: String?): List<Quote> {
        val list = _quotes.value.orEmpty()
        if (category.isNullOrBlank() || category == "All") return list
        return list.filter { it.category == category }
    }

    // ------------------------------------------------------------------------
    // GEMEINSAMER AUSWAHL-STATUS (z. B. für Detail-Screens)
    // ------------------------------------------------------------------------

    // Ausgewählter Autor, z. B. HomeFragment -> AuthorDetailsFragment
    private val _selectedAuthor = MutableLiveData<Author?>()
    val selectedAuthor: LiveData<Author?> = _selectedAuthor

    // Ausgewähltes Zitat, z. B. HomeFragment/CollectionFragment -> QuoteDetailsFragment
    private val _selectedQuote = MutableLiveData<Quote?>()
    val selectedQuote: LiveData<Quote?> = _selectedQuote

    // --- Setter-Methoden ---

    fun selectAuthor(author: Author) {
        _selectedAuthor.value = author
    }

    fun clearSelectedAuthor() {
        _selectedAuthor.value = null
    }

    fun selectQuote(quote: Quote) {
        _selectedQuote.value = quote
    }

    fun clearSelectedQuote() {
        _selectedQuote.value = null
    }
}