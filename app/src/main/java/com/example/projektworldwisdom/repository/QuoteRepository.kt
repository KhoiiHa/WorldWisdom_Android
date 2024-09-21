package com.example.projektworldwisdom.repository

import android.util.Log
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Image
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val apiService: WorldWisdomApiService
) {


    // Liefert alle verfügbaren Zitate, optional mit Limit
    suspend fun getAllQuotes(): List<Quote> {
        return try {
            val quotes = apiService.getAllQuotes() // API-Aufruf zur Abholung der Zitate
            insertQuotes(quotes) // Zitate in die lokale Datenbank einfügen
            quotes // Rückgabe der Zitate
        } catch (e: Exception) {
            // Fallback auf die lokale Datenbank
            withContext(Dispatchers.IO) { // Datenbankabfrage im IO-Kontext ausführen
                quoteDao.getAllQuotes() // Abruf der Zitate aus der lokalen Datenbank
            }
        }
    }

    // Liefert das Zitat des Tages
    suspend fun getQuoteOfTheDay(): Quote? {
        return try {
            // Abrufen des Zitats des Tages von der API
            val quote = apiService.getQuoteOfTheDay()
            // Das erste Zitat (einzelnes Zitat) verarbeiten
            insertQuote(quote)
            quote
        } catch (e: Exception) { // Fange alle Exceptions ab
            Log.e(
                "QuoteRepository",
                "Fehler beim Abrufen des Zitats des Tages von der API, Fallback auf lokale Datenbank: ${e.message}",
                e
            )
            withContext(Dispatchers.IO) {
                quoteDao.getQuoteOfTheDay() // Fallback auf lokale Datenbank
            }
        }
    }

    suspend fun getQuotesByAuthor(authorName: String): List<Quote> {
        return try {
            // Versuche, Zitate von der API abzurufen
            apiService.getQuotesByAuthor(authorName)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quotes by author from API", e)

            // Fallback: Hole Zitate aus der lokalen Datenbank
            try {
                quoteDao.getQuotesByAuthor(authorName)
            } catch (dbException: Exception) {
                Log.e("QuoteRepository", "Error getting quotes by author from local database", dbException)
                emptyList() // Gibt eine leere Liste zurück, falls auch der DB-Zugriff fehlschlägt
            }
        }
    }


    // Liefert eine Liste von Zitaten eines bestimmten Autors
    suspend fun searchQuotesByAuthor(authorName: String): List<Quote> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getQuotesByAuthor(authorName) // Direkt den Autorennamen verwenden
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Fehler beim Abrufen von Zitaten von der API: ${e.message}", e)
                quoteDao.searchQuotesByAuthor(authorName) // Lokale Suche implementieren
            }
        }
    }


    // Liefert Zitate, die mit dem angegebenen Schlüsselwort verknüpft sind
    suspend fun getQuotesByKeyword(keyword: String): List<Quote> {
        return try {
            // Versuche zuerst, die Zitate von der API abzurufen
            val apiQuotes = apiService.filterQuotesByKeyword(keyword = keyword)

            // Wenn die API erfolgreich war und Ergebnisse liefert, gib die Ergebnisse zurück
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

    suspend fun getAuthorByName(authorName: String): Author? {
        return try {
            quoteDao.getAuthorByName(authorName) // Annahme: es gibt eine entsprechende DAO-Methode
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting author by name", e)
            null // Rückgabe von null im Fehlerfall
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

    // Generiert ein Zitatbild basierend auf dem angegebenen Schlüsselwort
    suspend fun getImageByKeyword(keyword: String): Image? {
        return try {
            apiService.getImageByKeyword(keyword = keyword) // Bild basierend auf dem Schlüsselwort abrufen
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen des Zitatbildes von der API: ${e.message}", e)
            null // Rückgabe von null im Fehlerfall
        }
    }

    // Liefert eine Liste aller verfügbaren Autoren von der API.
    suspend fun fetchAuthors(): List<Author> {
        return try {
            apiService.getAuthors()
        } catch (e: Exception) {
            // Fehlerbehandlung
            Log.e("QuoteRepository", "Error fetching authors", e)
            emptyList() // Oder eine andere geeignete Fehlerbehandlung
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
            quoteDao.getAllQuotes()
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting all quotes", e)
            emptyList()
        }
    }

    // Löscht ein Zitat aus der lokalen Datenbank
    suspend fun deleteQuote(quote: Quote) {
        try {
            quoteDao.deleteQuote(quote)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting quote", e)
        }
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


}