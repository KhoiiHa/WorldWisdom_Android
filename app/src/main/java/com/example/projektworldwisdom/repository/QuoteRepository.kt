package com.example.projektworldwisdom.repository

import android.util.Log
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import com.example.projektworldwisdom.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val apiService: WorldWisdomApiService
) {


    // Liefert alle verfügbaren Zitate, optional mit Limit
    suspend fun getAllQuotes(limit: Int = 25): List<Quote> {
        return try {
            val quotes = apiService.getAllQuotes(limit)
            insertQuotes(quotes)
            quotes
        } catch (e: Exception) {
            // Fallback auf die lokale Datenbank
            withContext(Dispatchers.IO) { // Datenbankabfrage im IO-Kontext ausführen
                quoteDao.getAllQuotes(limit)
            }
        }
    }

    suspend fun searchQuotesByAuthor(author: String): List<Quote> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getQuotesByAuthor(authorName = author)
            } catch (e: Exception) {
                Log.e(
                    "QuoteRepository",
                    "Fehler beim Abrufen von Zitaten von der API, Fallback auf lokale Datenbank: ${e.message}",
                    e
                )
                quoteDao.searchQuotesByAuthor(author)
            }
        }
    }

    // Liefert ein zufälliges Zitat
    suspend fun getRandomQuote(): Quote? {
        return try {
            val response = apiService.getMultipleRandomQuotes(1)
            val quote = response.firstOrNull()
            if (quote != null) {
                insertQuote(quote) // Speichert das Zitat in der lokalen Datenbank
            }
            quote
        } catch (e: Exception) {
            // Fallback auf die lokale Datenbank
            quoteDao.getRandomQuote()
        }
    }

    // Liefert mehrere zufällige Zitate
    suspend fun getMultipleRandomQuotes(count: Int): List<Quote> {
        return try {
            val quotes = apiService.getMultipleRandomQuotes(count)
            insertQuotes(quotes) // Speichert die Zitate in der lokalen Datenbank
            quotes
        } catch (e: Exception) {
            // Fallback auf die lokale Datenbank
            quoteDao.getMultipleRandomQuotes(count)
        }
    }

    // Liefert Zitate, die mit dem angegebenen Schlüsselwort verknüpft sind
    suspend fun getQuotesByKeyword(keyword: String): List<Quote> {
        return try {
            // Versuche zuerst, die Zitate von der API abzurufen
            val apiQuotes = apiService.searchQuotesByKeyword(keyword)

            // Wenn die API erfolgreich war, gib die Ergebnisse zurück
            if (apiQuotes.isNotEmpty()) {
                apiQuotes
            } else {
                // Wenn die API keine Ergebnisse liefert, suche in der lokalen Datenbank
                quoteDao.getQuotesByKeyword(keyword)
            }
        } catch (e: Exception) {
            // Wenn die API nicht verfügbar ist oder ein Fehler auftritt, suche in der lokalen Datenbank
            Log.e(
                "QuoteRepository",
                "Fehler beim Abrufen von Zitaten von der API, Fallback auf lokale Datenbank: ${e.message}",
                e
            )
            quoteDao.getQuotesByKeyword(keyword)
        }
    }


    // Liefert das Zitat des Tages
    suspend fun getQuoteOfTheDay(): Quote? {
        return try {
            val response = apiService.getQuoteOfTheDay()
            val quote = response.firstOrNull()
            if (quote != null) {
                insertQuote(quote) // Speichert das Zitat in der lokalen Datenbank
            }
            quote
        } catch (e: Exception) {
            // Fallback auf die lokale Datenbank
            quoteDao.getQuoteOfTheDay()
        }
    }

    // Ruft einen Autor anhand seines Slugs aus der Datenbank ab
    suspend fun getAuthorBySlug(authorSlug: String): Author? {
        return try {
            quoteDao.getAuthorBySlug(authorSlug)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting author by slug", e)
            null
        }
    }


    // Liefert eine Liste aller verfügbaren Autoren
    suspend fun getAuthors(): List<Author> {
        return try {
            val authors =
                apiService.getAuthors() // Rufe getAuthors ohne API-Schlüssel als Query-Parameter auf
            insertAuthors(authors) // Speichern in der lokalen Datenbank
            authors
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching authors", e)
            // Fallback auf die lokale Datenbank
            quoteDao.getAllAuthors()
        }
    }

    // Fügt einen neuen Autor in die Datenbank ein
    suspend fun addAuthor(author: Author) {
        try {
            quoteDao.insertAuthor(author)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error inserting author", e)
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
    private suspend fun insertQuote(quote: Quote) {
        try {
            quoteDao.insertQuote(quote)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error inserting quote", e)
        }
    }


    // Speichert mehrere Zitate in der lokalen Datenbank
    suspend fun insertQuotes(quotes: List<Quote>) {
        withContext(Dispatchers.IO) {
            try {
                // Filtere ungültige Zitate heraus (z.B. solche ohne Inhalt)
                val validQuotes = quotes.filter { !it.content.isNullOrBlank() }
                quoteDao.insertQuotes(validQuotes)
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error inserting quotes", e)
            }
        }
    }

    // Speichert mehrere Autoren in der lokalen Datenbank
    private suspend fun insertAuthors(authors: List<Author>) {
        try {
            quoteDao.insertAuthors(authors)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error inserting authors", e)
        }
    }

    // Ruft Zitate nach Tag aus der lokalen Datenbank ab
    suspend fun getQuotesByKeywordFromLocal(tag: String): List<Quote> {
        return try {
            quoteDao.getQuotesByKeyword(tag)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quotes by tag", e)
            emptyList()
        }
    }

    // Ruft das Zitat des Tages aus der lokalen Datenbank ab
    suspend fun getQuoteOfTheDayFromLocal(): Quote? {
        return try {
            quoteDao.getQuoteOfTheDay()
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quote of the day", e)
            null
        }
    }

    // Ruft alle Zitate aus der lokalen Datenbank ab
    suspend fun getAllQuotesFromLocal(): List<Quote> {
        return try {
            quoteDao.getAllQuotes(15)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting all quotes", e)
            emptyList()
        }
    }

    // Löscht ein Zitat aus der lokalen Datenbank
    suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(quote)
    }

    // Ruft ein Zitat nach ID aus der lokalen Datenbank ab
    suspend fun getQuoteById(id: Int): Quote? {
        return try {
            quoteDao.getQuoteById(id)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quote by id", e)
            null
        }
    }

    // Löscht einen bestimmten Autor aus der lokalen Datenbank
    suspend fun deleteAuthor(author: Author) {
        try {
            quoteDao.deleteAuthor(author)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting author", e)
        }
    }

    // Löscht alle Autoren aus der lokalen Datenbank
    suspend fun deleteAllAuthors() {
        try {
            quoteDao.deleteAllAuthors()
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting all authors", e)
        }
    }

    suspend fun fetchAuthors(): List<Author> {
        return try {
            apiService.getAuthors()
        } catch (e: Exception) {
            // Fehlerbehandlung
            Log.e("QuoteRepository", "Error fetching authors", e)
            emptyList() // Oder eine andere geeignete Fehlerbehandlung
        }
    }
}