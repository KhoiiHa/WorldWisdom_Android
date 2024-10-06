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

    // Holt alle Zitate als LiveData (Datenbank)
    fun getAllQuotes(): LiveData<List<Quote>> {
        return quoteDao.getAllQuotes()
    }

    suspend fun refreshQuotes() {
        try {
            val quotes = apiService.getAllQuotes()

            // Alle Zitate in die Datenbank einfügen
            quoteDao.insertQuotes(quotes)

        } catch (e: Exception) {
            Log.e("QuoteRepository", "Failed to refresh quotes: ${e.message}")

        }
    }



    suspend fun getAuthorByName(authorName: String): Author? {
        return quoteDao.getAuthorByName(authorName)
    }

    // Funktion, um Zitate eines bestimmten Autors abzurufen
    suspend fun getQuotesByAuthorName(authorName: String): List<Quote> {
        return quoteDao.getQuotesByAuthorName(authorName)
    }

//    // Holt einen Autor anhand seines Tags als LiveData
//    fun getAuthorByTag(tag: String) {
//        Log.d("QuoteRepository", "Fetching author with tag: $tag")
//        val result = quoteDao.getAuthorByTag(tag)
//        Log.d("QuoteRepository", "Fetched author: ${result.value}")
//        _selectedAuthor.postValue(result.value)


    // Neue Methode, um ein zufälliges Zitat von einem bestimmten Autor abzurufen
    suspend fun getRandomQuoteByAuthor(authorName: String): Quote? {
        // Hole alle Zitate des Autors
        val quotes = quoteDao.getQuotesByAuthorName(authorName)

        // Wenn Zitate vorhanden sind, wähle zufällig eines aus
        return if (quotes.isNotEmpty()) {
            quotes.random()
        } else {
            null // Wenn keine Zitate vorhanden sind
        }
    }

    // Aktualisiert ein Zitat
    suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(quote)
    }


    // Funktion zum Abrufen aller Zitate aus der Datenbank
    fun getAllQuotesFromDatabase(): LiveData<List<Quote>> {
        return quoteDao.getAllQuotes()
    }

    // Gespeicherte Zitate abrufen (LiveData)
    fun getSavedQuotes(): LiveData<List<Quote>> {
        return quoteDao.getSavedQuotes() // Diese Methode gibt LiveData zurück
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

    // Speichert ein Zitat in der Datenbank
    suspend fun saveQuote(quote: Quote) {
        quoteDao.insertQuote(quote) // Zitat in die Datenbank einfügen
    }


    // Sucht Zitate anhand eines Stichworts als LiveData
    fun searchQuotesByKeyword(keyword: String): LiveData<List<Quote>> {
        return quoteDao.searchQuotesByKeyword(keyword)
    }

    // Holt Zitate anhand eines Stichworts als LiveData
    fun getQuotesByKeyword(keyword: String): LiveData<List<Quote>> {
        return quoteDao.getQuotesByKeyword(keyword)
    }

}



