package com.example.projektworldwisdom.repository

import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val apiService: WorldWisdomApiService
) {

    // Liefert ein zufälliges Zitat
    suspend fun getRandomQuote(): Quote? {
        return try {
            val response = apiService.getMultipleRandomQuotes(1) // Rufe ein einzelnes Zitat ab
            response.firstOrNull() // Nimmt das erste Zitat aus der Liste
        } catch (e: Exception) {
            null
        }
    }

    // Liefert mehrere zufällige Zitate (kann auch ein einzelnes Zitat liefern, wenn count = 1)
    suspend fun getMultipleRandomQuotes(count: Int): List<Quote> {
        return try {
            apiService.getMultipleRandomQuotes(count)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Liefert Zitate, die mit dem angegebenen Schlüsselwort verknüpft sind
    suspend fun getQuotesByKeyword(keyword: String): List<Quote> {
        return try {
            apiService.searchQuotesByKeyword(keyword) // Verwendet die Methode, um Zitate basierend auf dem Schlüsselwort zu suchen
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Liefert Zitate, die mit dem angegebenen Schlüsselwort verknüpft sind
    suspend fun searchQuotesByKeyword(keyword: String): List<Quote> {
        return try {
            apiService.searchQuotesByKeyword(keyword) // Verwendet die Methode, um Zitate basierend auf dem Schlüsselwort zu suchen
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Liefert alle verfügbaren Zitate, optional mit Limit
    suspend fun getAllQuotes(limit: Int = 10): List<Quote> {
        return try {
            apiService.getAllQuotes(limit) // Ruft eine bestimmte Anzahl von Zitaten ab
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Liefert das Zitat des Tages
    suspend fun getQuoteOfTheDay(): Quote? {
        return try {
            val response = apiService.getQuoteOfTheDay()
            response.firstOrNull() // Nimmt das erste Zitat aus der Liste
        } catch (e: Exception) {
            null
        }
    }

    // Liefert eine Liste aller verfügbaren Autoren
    suspend fun getAuthors(): List<Author> {
        return try {
            apiService.getAuthors() // Ruft eine Liste der Autoren ab
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Liefert die Liste der Schlüsselwörter
    suspend fun getKeywords(): List<Keyword> {
        return try {
            val response = apiService.getKeywords("70L87470TF537222S") // API-Schlüssel übergeben
            response.map { Keyword(it) } // Umwandeln der Strings in Keyword-Objekte
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Speichert ein einzelnes Zitat in der lokalen Datenbank
    suspend fun insertQuote(quote: Quote) {
        quoteDao.insertQuote(quote)
    }

    // Speichert mehrere Zitate in der lokalen Datenbank
    suspend fun insertQuotes(quotes: List<Quote>) {
        quoteDao.insertQuotes(quotes)
    }

    // Löscht ein Zitat aus der lokalen Datenbank
    suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(quote)
    }

    // Ruft ein Zitat nach ID aus der lokalen Datenbank ab
    suspend fun getQuoteById(id: Int): Quote? {
        return quoteDao.getQuoteById(id)
    }
}