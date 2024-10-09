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

    suspend fun refreshQuotes() {
        try {
            val quotes = apiService.getAllQuotes()

            // Alle Zitate in die Datenbank einfügen
            quoteDao.insertQuotes(quotes)

        } catch (e: Exception) {
            Log.e("QuoteRepository", "Failed to refresh quotes: ${e.message}")

        }
    }

    suspend fun refreshQuoteOfTheDay() {
        try {
            // Hole alle Zitate von der API
            val quotes = apiService.getAllQuotes()
            Log.d("QuoteRepository", "Zitate von API abgerufen: ${quotes.size} Zitate gefunden.")

            val quoteOfTheDay = quotes.random() // Wähle zufällig ein Zitat aus
            Log.d("QuoteRepository", "Neues Zitat des Tages: ${quoteOfTheDay.content}")

            // Setze alle bestehenden Zitate des Tages zurück
            quoteDao.resetAllQuotesOfTheDay()

            // Markiere das neue Zitat des Tages
            val updatedQuoteOfTheDay = quoteOfTheDay.copy(isQuoteOfTheDay = true)

            // Füge das neue Zitat in die Datenbank ein (Einzelnes Zitat)
            quoteDao.insertQuote(updatedQuoteOfTheDay)
            Log.d("QuoteRepository", "Neues Zitat des Tages in die Datenbank eingefügt.")

        } catch (e: Exception) {
            Log.e("QuoteRepository", "Failed to refresh quotes: ${e.message}")
        }
    }


    // Funktion, um Zitate eines bestimmten Autors abzurufen
    suspend fun getQuotesByAuthorName(authorName: String): List<Quote> {
        return quoteDao.getQuotesByAuthorName(authorName)
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
        return quoteDao.getSavedQuotes()
    }

    // Löscht ein Zitat anhand der ID
    suspend fun deleteQuoteById(id: Int) {
        quoteDao.deleteQuoteById(id)
    }


    // Holt das Zitat des Tages als LiveData
    fun getQuoteOfTheDay(): LiveData<Quote> {
        return quoteDao.getQuoteOfTheDay()
    }

    // Speichert ein Zitat in der Datenbank
    suspend fun saveQuote(quote: Quote) {
        quoteDao.insertQuote(quote) // Zitat in die Datenbank einfügen
    }


    // Holt Zitate anhand eines Stichworts als LiveData
    fun getQuotesByKeyword(keyword: String): LiveData<List<Quote>> {
        return quoteDao.getQuotesByKeyword(keyword)
    }

}



