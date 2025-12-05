package com.example.projektworldwisdom.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.projektworldwisdom.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(
    private val quoteApi: WorldWisdomApiService
) {

    private val _downloading = MutableLiveData(false)
    val downloading: LiveData<Boolean> = _downloading

    /**
     * Holt ALLE Zitate von der Mockoon-API.
     * Wird von Home-Liste und Random-Quote gemeinsam verwendet.
     */
    suspend fun getAllQuotes(): List<Quote> {
        _downloading.postValue(true)
        return withContext(Dispatchers.IO) {
            try {
                quoteApi.getAllQuotes()
            } catch (e: Exception) {
                // TODO: Logging bei Bedarf
                emptyList()
            } finally {
                _downloading.postValue(false)
            }
        }
    }

    /**
     * Liefert ein einziges zufälliges Zitat.
     * Nutzt intern getAllQuotes().
     */
    suspend fun getRandomQuote(): Quote? {
        val quotes = getAllQuotes()
        return quotes.randomOrNull()
    }
}