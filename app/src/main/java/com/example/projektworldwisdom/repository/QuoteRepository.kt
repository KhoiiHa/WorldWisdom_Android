package com.example.projektworldwisdom.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.mockApi.MockApi
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApi
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val apiService: WorldWisdomApiService
) {

    // Holt alle Zitate als LiveData (Datenbank)
    fun getAllQuotes(): LiveData<List<Quote>> {
        return quoteDao.getAllQuotes()
    }

    fun getAllAuthors(): LiveData<List<Author>> {
        return quoteDao.getAllAuthors()
    }

    suspend fun refreshQuotes() {
        val quotes = apiService.getAllQuotes()
        val authors = apiService.getAllAuthors()
        // Alle Zitate in die Datenbank einfügen
        quoteDao.insertQuotes(quotes) // Diese Methode akzeptiert nun eine Liste
        // Alle Autoren in die Datenbank einfügen
        quoteDao.insertAuthors(authors) // Diese Methode akzeptiert ebenfalls eine Liste
    }

    // Methode zum Speichern eines Zitats in der Datenbank
    suspend fun saveQuote(quote: Quote) {
        quoteDao.insertQuote(quote)  // Diese Methode muss in deinem Dao definiert sein
    }



    // Holt einen Autor anhand seiner ID als LiveData
    fun getAuthorById(id: Int): LiveData<Author> {
        return quoteDao.getAuthorById(id)
    }
    // Löscht ein Zitat anhand der ID
    suspend fun deleteQuoteById(id: Int) {
        quoteDao.deleteQuoteById(id)
    }

    // Holt ein Zitat anhand der ID als LiveData
    fun getQuoteById(id: Int): LiveData<Quote> {
        return quoteDao.getQuoteById(id)
    }

    // Holt das Zitat des Tages als LiveData
    fun getQuoteOfTheDay(): LiveData<Quote> {
        return quoteDao.getQuoteOfTheDay()
    }

    // Holt alle gespeicherten Zitate als LiveData
    fun getSavedQuotes(): LiveData<List<Quote>> {
        return quoteDao.getSavedQuotes()
    }

    // Holt Zitate anhand des Autornamens als LiveData
    fun getQuotesByAuthorName(authorName: String): LiveData<List<Quote>> {
        return quoteDao.getQuotesByAuthorName(authorName)
    }

    // Sucht Zitate anhand eines Stichworts als LiveData
    fun searchQuotesByKeyword(keyword: String): LiveData<List<Quote>> {
        return quoteDao.searchQuotesByKeyword(keyword)
    }

    // Holt Zitate anhand eines Stichworts als LiveData
    fun getQuotesByKeyword(keyword: String): LiveData<List<Quote>> {
        return quoteDao.getQuotesByKeyword(keyword)
    }

    // Aktualisiert ein Zitat
    suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(quote)
    }
}