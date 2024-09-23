package com.example.projektworldwisdom.repository

import android.util.Log
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.mockApi.MockApi
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Image
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(
    private val quoteDao: QuoteDao,
    private val apiService: WorldWisdomApiService
) {



    suspend fun getAllQuotes(): List<Quote> {
        return try {
            val quotes = MockApi.getAllQuotes()
            Log.d("QuoteRepository", "Mock API Response for all quotes: $quotes")
            insertQuotes(quotes)
            quotes
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen aller Zitate von der Mock-API", e)

            // Fallback auf die lokale Datenbank
            val localQuotes = withContext(Dispatchers.IO) {
                quoteDao.getAllQuotes() // Abrufen der Zitate aus der Datenbank
            }

            if (localQuotes.isNotEmpty()) {
                Log.d("QuoteRepository", "Returning local quotes: $localQuotes")
            } else {
                Log.e("QuoteRepository", "No quotes available in local database")
            }

            localQuotes // Rückgabe der lokalen Zitate oder einer leeren Liste
        }
    }


    // Liefert das Zitat des Tages
    suspend fun getQuoteOfTheDay(): Quote? {
        return try {
            // Abrufen des Zitats des Tages von der API
            val quotes = MockApi.getQuoteOfTheDay() // List<Quote>

            // Log für die gesamte API-Antwort
            Log.d("QuoteRepository", "API Response: $quotes")

            if (quotes.isNotEmpty()) {
                val quote = quotes[0] // Zugriff auf das erste Zitat
                Log.d("QuoteRepository", "Received quote: ${quote.content}") // Log für das Zitat
                insertQuote(quote) // Gültiges Zitat in die Datenbank einfügen
                quote
            } else {
                Log.e("QuoteRepository", "No quotes received from API") // Log, wenn keine Zitate zurückgegeben werden
                null // Falls keine Zitate zurückgegeben werden
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen des Zitats des Tages von der API", e)
            val localQuote = quoteDao.getQuoteOfTheDay() // Fallback auf lokale Datenbank
            if (localQuote != null) {
                Log.d("QuoteRepository", "Returning local quote: ${localQuote.content}")
            } else {
                Log.e("QuoteRepository", "No quote available in local database")
            }
            localQuote // Rückgabe des Zitats aus der lokalen Datenbank (oder null)
        }
    }

    // Liefert eine Liste aller verfügbaren Autoren
    suspend fun getAllAuthors(): List<Author> {
        return try {
            // Abrufen der Autoren von der Mock-API
            val authors = MockApi.getAllAuthors() // List<Author>

            // Log für die gesamte API-Antwort
            Log.d("AuthorRepository", "API Response for all authors: $authors")

            if (authors.isNotEmpty()) {
                Log.d("AuthorRepository", "Received authors: $authors") // Log für die Autoren
                authors
            } else {
                Log.e("AuthorRepository", "No authors received from API") // Log, wenn keine Autoren zurückgegeben werden
                emptyList() // Rückgabe einer leeren Liste, wenn keine Autoren vorhanden sind
            }
        } catch (e: Exception) {
            Log.e("AuthorRepository", "Fehler beim Abrufen der Autoren von der API", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Liefert ein zufälliges inspirierendes Bild
    suspend fun getRandomInspirationalImage(): String? {
        return try {
            // Abrufen eines zufälligen inspirierenden Bildes von der Mock-API
            val imageUrl = MockApi.getRandomInspirationalImage() // String

            // Log für die gesamte API-Antwort
            Log.d("QuoteRepository", "API Response for random image: $imageUrl")

            if (imageUrl.isNotEmpty()) {
                Log.d("QuoteRepository", "Received image URL: $imageUrl") // Log für das Bild
                imageUrl
            } else {
                Log.e("QuoteRepository", "No image URL received from API") // Log, wenn keine URL zurückgegeben wird
                null // Falls keine URL zurückgegeben wird
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen des zufälligen inspirierenden Bildes von der API", e)
            // Fallback kann hier eine lokale Bild-URL oder eine Standard-URL sein
            val localImageUrl = "default_image_url" // Hier eine geeignete Standard-URL setzen
            Log.d("QuoteRepository", "Returning default image URL: $localImageUrl")
            localImageUrl // Rückgabe der Standard-URL
        }
    }

    suspend fun getAuthorImage(authorName: String): String? {
        return try {
            // Abrufen des Zitat-Bilds von der Mock API
            val imageUrl = MockApi.getAuthorImage(authorName)

            Log.d("QuoteRepository", "API Response for author image $authorName: $imageUrl")

            imageUrl // Rückgabe der Bild-URL
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen des Bildes für Autor $authorName", e)
            null // Rückgabe von null im Fehlerfall
        }
    }

    suspend fun getQuotesByAuthor(authorName: String): List<Quote> {
        return try {
            // Abrufen der Zitate des bestimmten Autors von der Mock API
            val quotes = MockApi.getQuotesByAuthor(authorName)

            Log.d("QuoteRepository", "API Response for author $authorName: $quotes")

            if (quotes.isNotEmpty()) {
                quotes // Rückgabe der gefundenen Zitate
            } else {
                Log.e("QuoteRepository", "No quotes found for author $authorName")
                emptyList() // Rückgabe einer leeren Liste, wenn keine Zitate gefunden wurden
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen der Zitate für Autor $authorName", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    suspend fun searchQuotesByAuthor(authorName: String): List<Quote> {
        return try {
            // Abrufen der Zitate eines bestimmten Autors von der Mock API
            val quotes = MockApi.getQuotesByAuthor(authorName)
            Log.d("QuoteRepository", "Fetched ${quotes.size} quotes for author '$authorName'")
            quotes
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen der Zitate für den Autor '$authorName'", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }


    suspend fun getQuotesByKeyword(keyword: String): List<Quote> {
        return try {
            // Abrufen der gefilterten Zitate von der Mock API mit einem einzelnen Keyword
            val filteredQuotes = MockApi.filterQuotesByKeywords(listOf(keyword))

            Log.d("QuoteRepository", "Filtered quotes by keyword '$keyword': $filteredQuotes")
            filteredQuotes
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Filtern der Zitate nach dem Schlüsselwort '$keyword'", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Generiert ein Zitatbild basierend auf dem angegebenen Schlüsselwort
    suspend fun getImageByKeyword(keyword: String): Image? {
        return try {
            val image = MockApi.getImageByKeyword(keyword) // Bild basierend auf dem Schlüsselwort von der MockAPI abrufen
            Log.d("QuoteRepository", "Mock API Response for image: $image") // Log für die MockAPI-Antwort
            image
        } catch (e: Exception) {
            Log.e(
                "QuoteRepository",
                "Fehler beim Abrufen des Zitatbildes von der MockAPI, Fallback auf null: ${e.message}",
                e
            )
            null // Rückgabe von null im Fehlerfall
        }
    }

    // Ruft einen Autor anhand seines Namens ab
    suspend fun getAuthorByName(authorName: String): Author? {
        return try {
            quoteDao.getAuthorByName(authorName) // Abfrage in der lokalen Datenbank
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching author by name", e)
            null // Rückgabe von null im Fehlerfall
        }
    }


    // Liefert eine Liste aller verfügbaren Autoren von der API.
    suspend fun fetchAuthors(): List<Author> {
        return try {
            val authors = apiService.getAuthors() // Autoren von der API abrufen
            Log.d("QuoteRepository", "API Response for authors: $authors") // Log für die API-Antwort
            authors
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching authors, fallback to empty list: ${e.message}", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Fügt einen neuen Autor in die Datenbank ein und gibt zurück, ob das Einfügen erfolgreich war
    suspend fun addAuthor(author: Author): Boolean {
        return if (!author.name.isNullOrBlank()) {
            try {
                quoteDao.insertAuthor(author) // Autor in die Datenbank einfügen
                Log.d("QuoteRepository", "Author added: $author") // Log für den erfolgreich hinzugefügten Autor
                true // Erfolgreiches Einfügen
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error inserting author: ${e.message}", e) // Fehlermeldung mit spezifischem Fehler
                false // Fehler beim Einfügen
            }
        } else {
            Log.e("QuoteRepository", "Author name is null or blank, not inserting.") // Log für ungültigen Autor
            false // Ungültiger Autor
        }
    }


    // Speichert ein einzelnes Zitat in der lokalen Datenbank und gibt zurück, ob das Einfügen erfolgreich war
    private suspend fun insertQuote(quote: Quote): Boolean {
        return if (!quote.content.isNullOrBlank()) {
            try {
                quoteDao.insertQuote(quote) // Zitat in die Datenbank einfügen
                Log.d("QuoteRepository", "Quote inserted successfully: ${quote.content}") // Log für erfolgreichen Insert
                true // Erfolgreiches Einfügen
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error inserting quote: ${e.message}", e) // Fehlermeldung mit spezifischem Fehler
                false // Fehler beim Einfügen
            }
        } else {
            Log.e("QuoteRepository", "Quote content is null or blank, not inserting.") // Log für leeres Zitat
            false // Ungültiges Zitat
        }
    }


    // Speichert mehrere Zitate in der lokalen Datenbank und gibt zurück, wie viele erfolgreich gespeichert wurden
    suspend fun insertQuotes(quotes: List<Quote>): Int {
        return withContext(Dispatchers.IO) {
            try {
                // Filtere ungültige Zitate heraus (z.B. solche ohne Inhalt)
                val validQuotes = quotes.filter { !it.content.isNullOrBlank() } // Prüfen auf null und leer

                if (validQuotes.isNotEmpty()) {
                    quoteDao.insertQuotes(validQuotes) // Gültige Zitate in die Datenbank einfügen
                    Log.d("QuoteRepository", "Inserted ${validQuotes.size} valid quotes.") // Log für erfolgreiche Einfügung
                    validQuotes.size // Rückgabe der Anzahl erfolgreich eingefügter Zitate
                } else {
                    Log.e("QuoteRepository", "No valid quotes to insert.") // Log, wenn keine gültigen Zitate vorhanden sind
                    0 // Keine gültigen Zitate, Rückgabe 0
                }
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error inserting quotes: ${e.message}", e) // Fehlermeldung mit spezifischem Fehler
                0 // Rückgabe 0 im Fehlerfall
            }
        }
    }


    // Speichert mehrere Autoren in der lokalen Datenbank und gibt zurück, wie viele erfolgreich gespeichert wurden
    suspend fun insertAuthors(authors: List<Author>): Int {
        return withContext(Dispatchers.IO) {
            try {
                // Filtere ungültige Autoren heraus (z.B. solche ohne Namen)
                val validAuthors = authors.filter { !it.name.isNullOrBlank() } // Prüfen auf null und leer

                if (validAuthors.isNotEmpty()) {
                    quoteDao.insertAuthors(validAuthors) // Gültige Autoren in die Datenbank einfügen
                    Log.d("QuoteRepository", "Inserted ${validAuthors.size} valid authors.") // Log für erfolgreiche Einfügung
                    validAuthors.size // Rückgabe der Anzahl erfolgreich eingefügter Autoren
                } else {
                    Log.e("QuoteRepository", "No valid authors to insert.") // Log, wenn keine gültigen Autoren vorhanden sind
                    0 // Keine gültigen Autoren, Rückgabe 0
                }
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error inserting authors: ${e.message}", e) // Fehlermeldung mit spezifischem Fehler
                0 // Rückgabe 0 im Fehlerfall
            }
        }
    }

    // Ruft Zitate nach Tag aus der lokalen Datenbank ab
    suspend fun getQuotesByKeywordFromLocal(tag: String): List<Quote> {
        return try {
            // Überprüfen, ob der Tag nicht null oder leer ist
            if (tag.isNotBlank()) {
                quoteDao.getQuotesByKeyword(tag) // Zitate aus der Datenbank abrufen
            } else {
                Log.e("QuoteRepository", "Tag is null or blank, returning empty list.")
                emptyList() // Rückgabe einer leeren Liste, wenn der Tag ungültig ist
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quotes by tag", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Ruft das Zitat des Tages aus der lokalen Datenbank ab
    suspend fun getQuoteOfTheDayFromLocal(): Quote? {
        return try {
            val quote = quoteDao.getQuoteOfTheDay()
            if (quote == null) {
                Log.w("QuoteRepository", "No quote of the day found in local database.") // Warnung, wenn kein Zitat gefunden wird
            }
            quote
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quote of the day", e) // Fehlermeldung im Fehlerfall
            null
        }
    }

    // Ruft alle Zitate aus der lokalen Datenbank ab
    suspend fun getAllQuotesFromLocal(): List<Quote> {
        return try {
            val quotes = quoteDao.getAllQuotes() // Alle Zitate abrufen
            if (quotes.isEmpty()) {
                Log.w("QuoteRepository", "No quotes found in local database.") // Warnung, wenn keine Zitate gefunden werden
            }
            quotes // Rückgabe der gefundenen Zitate
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting all quotes", e) // Fehlermeldung im Fehlerfall
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Löscht ein Zitat aus der lokalen Datenbank
    suspend fun deleteQuote(quote: Quote) {
        try {
            quoteDao.deleteQuote(quote) // Zitat aus der Datenbank löschen
            Log.d("QuoteRepository", "Quote deleted successfully: ${quote.content}") // Log für erfolgreichen Löschvorgang
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting quote", e) // Fehlermeldung im Fehlerfall
        }
    }

    // Ruft ein Zitat nach ID aus der lokalen Datenbank ab
    suspend fun getQuoteById(id: Int): Quote? {
        return try {
            val quote = quoteDao.getQuoteById(id) // Zitat nach ID abrufen
            if (quote != null) {
                Log.d("QuoteRepository", "Quote retrieved successfully: ${quote.content}") // Log für erfolgreiches Abrufen
            } else {
                Log.e("QuoteRepository", "No quote found for id: $id") // Log, wenn kein Zitat gefunden wurde
            }
            quote // Rückgabe des Zitats oder null
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quote by id", e) // Fehlermeldung im Fehlerfall
            null // Rückgabe von null im Fehlerfall
        }
    }

    // Löscht einen bestimmten Autor aus der lokalen Datenbank
    suspend fun deleteAuthor(author: Author) {
        try {
            val rowsDeleted = quoteDao.deleteAuthor(author) // Den Rückgabewert speichern
            Log.d("QuoteRepository", "Author deleted successfully: ${author.name}, Rows deleted: $rowsDeleted") // Log für den Löschvorgang
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting author", e) // Fehlermeldung im Fehlerfall
        }
    }

    // Löscht alle Autoren aus der lokalen Datenbank
    suspend fun deleteAllAuthors() {
        try {
            val rowsDeleted = quoteDao.deleteAllAuthors() // Den Rückgabewert speichern
            Log.d("QuoteRepository", "All authors deleted successfully, Rows deleted: $rowsDeleted") // Log für den Löschvorgang
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting all authors", e) // Fehlermeldung im Fehlerfall
        }
    }

}


