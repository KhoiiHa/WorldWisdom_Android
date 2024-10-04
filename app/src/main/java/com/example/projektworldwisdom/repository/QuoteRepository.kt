package com.example.projektworldwisdom.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val apiService: WorldWisdomApiService
) {

    private var _selectedAuthor = MutableLiveData<Author>()
    val selectedAuthor: LiveData<Author> get() = _selectedAuthor


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
        quoteDao.insertQuotes(quotes)
        // Alle Autoren in die Datenbank einfügen
        quoteDao.insertAuthors(authors)
    }


    // Neue Methode, um ein zufälliges Zitat von einem bestimmten Autor abzurufen
    suspend fun getRandomQuoteByAuthor(authorId: Int): Quote? {
        // Hole alle Zitate des Autors
        val quotes = quoteDao.getQuotesByAuthor(authorId)

        // Wenn Zitate vorhanden sind, wähle zufällig eines aus
        return if (quotes.isNotEmpty()) {
            quotes.random()
        } else {
            null // Wenn keine Zitate vorhanden sind
        }
    }



    // Methode zum Speichern eines Zitats in der Datenbank
    suspend fun saveQuote(quote: Quote) {
        quoteDao.insertQuote(quote)
    }


    // Holt einen Autor anhand seiner ID als LiveData
    fun getAuthorById(id: Int) {
        Log.d("QuoteRepository", "Fetching author with ID: $id")
        val result = quoteDao.getAuthorById("jobs")
        Log.d("QuoteRepository", "Fetched author: ${result.value}")
        _selectedAuthor.postValue(result.value)
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