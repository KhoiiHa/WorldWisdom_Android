package com.example.projektworldwisdom.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.QuoteRepository
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val quoteApi: WorldWisdomApiService = WorldWisdomApi.retrofitService
    private val quoteRepository: QuoteRepository = QuoteRepository(quoteApi)

    // ------------------------------------------------------------------------
    // FAVORITES (persistiert, aber bewusst "lightweight")
    // ------------------------------------------------------------------------

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _favoriteKeys = MutableLiveData<Set<String>>(readFavoriteKeys())
    val favoriteKeys: LiveData<Set<String>> = _favoriteKeys

    /**
     * Favoriten-Liste als LiveData, abgeleitet aus Quotes + favoriteKeys.
     */
    private val _favoriteQuotes = MediatorLiveData<List<Quote>>().apply {
        value = emptyList()
    }
    val favoriteQuotes: LiveData<List<Quote>> = _favoriteQuotes

    // ------------------------------------------------------------------------
    // ZENTRALE QUOTE-DATEN (für HomeFragment + CollectionFragment)
    // ------------------------------------------------------------------------

    private var allQuotes: List<Quote> = emptyList()

    private val _quotes = MutableLiveData<List<Quote>>(emptyList())
    val quotes: LiveData<List<Quote>> = _quotes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    init {
        // Beobachte Änderungen → UI-Listen immer konsistent halten
        _favoriteQuotes.addSource(_quotes) { recomputeFavoriteQuotes() }
        _favoriteQuotes.addSource(_favoriteKeys) {
            // Wenn Favoriten wechseln, müssen wir auch die Quote-Liste (Stern-Status) aktualisieren.
            publishQuotesWithFavorites()
            recomputeFavoriteQuotes()
        }

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
                allQuotes = result
                publishQuotesWithFavorites()
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
     */
    fun getDailyQuote(): Quote? {
        val current = _quotes.value.orEmpty()
        return current.firstOrNull()
    }

    /**
     * Hilfsfunktion (optional): Filtert die Liste nach Kategorie.
     * category == null oder "All" → komplette Liste.
     */
    fun getQuotesByCategory(category: String?): List<Quote> {
        val list = _quotes.value.orEmpty()
        val normalized = category?.trim().orEmpty()
        if (normalized.isBlank() || normalized.equals("All", ignoreCase = true) || normalized.equals("Alle", ignoreCase = true)) {
            return list
        }
        return list.filter { it.category == normalized }
    }

    // ------------------------------------------------------------------------
    // FAVORITES API (für Adapter / UI)
    // ------------------------------------------------------------------------

    /**
     * True, wenn die Quote aktuell als Favorit gespeichert ist (über favoriteKeys).
     */
    fun isFavorite(quote: Quote): Boolean {
        return _favoriteKeys.value.orEmpty().contains(quote.favoriteKey)
    }

    /**
     * Toggle Favorit-Status (persistiert sofort).
     */
    fun toggleFavorite(quote: Quote) {
        val newState = !isFavorite(quote)
        setFavorite(quote, newState)
    }

    /**
     * Setzt den Favoriten-Zielzustand explizit (true = speichern, false = entfernen).
     * → Genau das brauchst du für "Stern nochmal klicken = Reset/Undo".
     */
    fun setFavorite(quote: Quote, isFavorite: Boolean) {
        val current = _favoriteKeys.value.orEmpty().toMutableSet()
        val key = quote.favoriteKey

        val changed = if (isFavorite) {
            current.add(key)
        } else {
            current.remove(key)
        }

        if (!changed) return

        // Wichtig: immutable Set publishen (verhindert versehentliche Mutationen)
        val newSet: Set<String> = current.toSet()
        saveFavoriteKeys(newSet)
        _favoriteKeys.value = newSet
    }

    // ------------------------------------------------------------------------
    // INTERNAL HELPERS
    // ------------------------------------------------------------------------

    private fun publishQuotesWithFavorites() {
        val favorites = _favoriteKeys.value.orEmpty()
        _quotes.value = allQuotes.map { it.copy(isFavorite = favorites.contains(it.favoriteKey)) }
    }

    private fun recomputeFavoriteQuotes() {
        val favorites = _favoriteKeys.value.orEmpty()
        val currentQuotes = _quotes.value.orEmpty()

        // Robust: derive from keys (prevents edge-cases where isFavorite flags are stale)
        _favoriteQuotes.value = currentQuotes.filter { favorites.contains(it.favoriteKey) }
    }

    private fun readFavoriteKeys(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()
    }

    private fun saveFavoriteKeys(keys: Set<String>) {
        // SharedPreferences erwartet ein MutableSet, aber wir speichern immer defensiv eine Kopie.
        prefs.edit().putStringSet(KEY_FAVORITES, keys.toMutableSet()).apply()
    }

    // ------------------------------------------------------------------------
    // GEMEINSAMER AUSWAHL-STATUS (z. B. für Detail-Screens)
    // ------------------------------------------------------------------------

    private val _selectedAuthor = MutableLiveData<Author?>()
    val selectedAuthor: LiveData<Author?> = _selectedAuthor

    private val _selectedQuote = MutableLiveData<Quote?>()
    val selectedQuote: LiveData<Quote?> = _selectedQuote

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

    companion object {
        private const val PREFS_NAME = "worldwisdom_prefs"
        private const val KEY_FAVORITES = "favorite_keys"
    }
}