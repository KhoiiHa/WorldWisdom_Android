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


    suspend fun getRandomQuote(): Quote? {
        _downloading.postValue(true)
        return withContext(Dispatchers.IO) {
            try {
                val quotes = quoteApi.getAllQuotes()
                quotes.randomOrNull()
            } catch (e: Exception) {
                // TODO: Logging oder spezifischere Fehlerbehandlung bei Bedarf
                null
            } finally {
                _downloading.postValue(false)
            }
        }
    }
}