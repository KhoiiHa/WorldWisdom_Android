package com.example.projektworldwisdom.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.projektworldwisdom.local.QuoteDao
import com.example.projektworldwisdom.mockApi.MockApi
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Image
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(private val quoteDao: QuoteDao) {


    // Liefert eine Liste aller Zitate
    suspend fun getAllQuotes(): List<Quote> {
        return try {
            // Abrufen aller Zitate von der Mock API
            val quotes = MockApi.getAllQuotes()
            Log.d("QuoteRepository", "Mock API Response for all quotes: $quotes")
            insertQuotes(quotes) // Speichere die Zitate in der lokalen Datenbank
            quotes // Rückgabe der Zitate von der API
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
                Log.e(
                    "QuoteRepository",
                    "No quotes received from API"
                ) // Log, wenn keine Zitate zurückgegeben werden
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

    // Neue Funktion, um alle verfügbaren Schlüsselwörter abzurufen
    suspend fun getAvailableKeywords(): List<String> {
        return try {
            // Angenommen, die MockApi hat eine Methode getAvailableKeywords()
            val keywords = MockApi.getAvailableKeywords() // Beispiel für den API-Aufruf
            Log.d("QuoteRepository", "Available keywords: $keywords")
            keywords
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error loading available keywords", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
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
                authors // Rückgabe der Autoren
            } else {
                Log.e("AuthorRepository", "No authors received from API") // Log, wenn keine Autoren zurückgegeben werden
                emptyList() // Rückgabe einer leeren Liste, wenn keine Autoren vorhanden sind
            }
        } catch (e: Exception) {
            Log.e("AuthorRepository", "Fehler beim Abrufen der Autoren von der API", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Methode zum Abrufen des Tags eines Autors anhand seines Namens
    suspend fun getAuthorTagByName(authorName: String?): String? {
        if (authorName.isNullOrEmpty()) return null

        return try {
            // Abrufe den Autor und extrahiere den Tag
            val author = getAuthorByName(authorName)
            author?.tag // Gib den Tag des Autors zurück, falls vorhanden
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching author tag by name", e)
            null // Rückgabe von null im Fehlerfall
        }
    }

    // Liefert eine Liste aller verfügbaren Tags
    suspend fun getAllTags(): List<String> {
        return try {
            // Hole alle Autoren und extrahiere die Tags
            getAllAuthors().map { it.tag }.distinct().sorted() // Extrahiere die Tags aus der Liste der Autoren
        } catch (e: Exception) {
            Log.e("TagRepository", "Fehler beim Abrufen der Tags von der API", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Liefert eine Liste aller verfügbaren Keywords
    suspend fun getAllKeywords(): List<String> {
        return try {
            // Nutze die bereits vorhandene Funktion getAvailableKeywords() aus MockAPI
            MockApi.getAvailableKeywords() // List<String>
        } catch (e: Exception) {
            Log.e("KeywordRepository", "Fehler beim Abrufen der Keywords von der API", e)
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
                Log.e(
                    "QuoteRepository",
                    "No image URL received from API"
                ) // Log, wenn keine URL zurückgegeben wird
                null // Falls keine URL zurückgegeben wird
            }
        } catch (e: Exception) {
            Log.e(
                "QuoteRepository",
                "Fehler beim Abrufen des zufälligen inspirierenden Bildes von der API",
                e
            )
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

    suspend fun getQuotesByQuery(query: String): List<Quote> {
        return try {
            // Rufe alle Zitate von der Mock API ab
            val allQuotes = MockApi.getAllQuotes() // Diese Methode sollte alle Zitate zurückgeben
            val filteredQuotes = allQuotes.filter { quote ->
                // Nutze die korrekten Eigenschaften aus deinem Modell
                quote.content?.contains(query, ignoreCase = true) == true ||
                        quote.authorName?.contains(query, ignoreCase = true) == true ||
                        quote.keywords.any { keyword -> keyword.contains(query, ignoreCase = true) }
            }

            Log.d("QuoteRepository", "Filtered quotes by query '$query': $filteredQuotes")
            filteredQuotes // Rückgabe der gefilterten Zitate
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error filtering quotes by query '$query'", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    suspend fun searchQuotesByAuthorAndKeywords(authorName: String?, keywords: List<String>): List<Quote> {
        return try {
            val matchingQuotes = mutableListOf<Quote>()

            // Fall 1: Suche nach Autor und Keywords
            if (!authorName.isNullOrEmpty() || !keywords.isNullOrEmpty()) {
                if (keywords.isNotEmpty()) {
                    // Wenn Keywords vorhanden sind, iteriere über die Keywords und suche
                    keywords.forEach { keyword ->
                        val quotesByAuthorAndKeyword = quoteDao.searchQuotesByAuthorAndKeyword(authorName, keyword)
                        matchingQuotes.addAll(quotesByAuthorAndKeyword)
                    }
                } else {
                    // Suche nur nach Autor, wenn keine Keywords vorhanden sind
                    val quotesByAuthor = authorName?.let { quoteDao.searchQuotesByAuthor(it) }
                    if (quotesByAuthor != null) {
                        matchingQuotes.addAll(quotesByAuthor)
                    }
                }
            }

            // Fall 2: Wenn nur Keywords vorhanden sind und kein Autor angegeben wurde
            if (keywords.isNotEmpty() && authorName.isNullOrEmpty()) {
                keywords.forEach { keyword ->
                    val quotesByKeyword = quoteDao.searchQuotesByKeyword(keyword)
                    matchingQuotes.addAll(quotesByKeyword)
                }
            }

            // Entferne doppelte Zitate, falls vorhanden, und gib die Liste zurück
            matchingQuotes.distinct()
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching quotes by author or keyword. Author: $authorName, Keywords: $keywords", e)
            emptyList() // Fehlerbehandlung: leere Liste zurückgeben
        }
    }

    suspend fun getQuotesByAuthor(authorName: String): List<Quote> {
        return try {
            // Abrufen der Zitate des bestimmten Autors von der Mock API
            val quotes = MockApi.getQuotesByAuthor(authorName)

            Log.d("QuoteRepository", "API Response for author $authorName: $quotes")

            // Überprüfen, ob die Liste der Zitate nicht leer ist
            if (quotes.isNotEmpty()) {
                quotes // Rückgabe der gefundenen Zitate
            } else {
                Log.e("QuoteRepository", "No quotes found for author $authorName")
                emptyList() // Rückgabe einer leeren Liste, wenn keine Zitate gefunden wurden
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching quotes for author $authorName", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Sucht Zitate anhand des Autorennamens
    suspend fun searchQuotesByAuthor(authorName: String): List<Quote> {
        return try {
            // Verwende die Funktion getQuotesByAuthor für Konsistenz
            getQuotesByAuthor(authorName)
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Suchen der Zitate für Autor: $authorName", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }


    // Liefert Zitate basierend auf einem bestimmten Tag
    suspend fun getQuotesByTag(tag: String): List<Quote> {
        return try {
            // Abrufen der Autoren, die den passenden Tag haben
            val authorsWithTag = quoteDao.getAuthorsByTag(tag)

            if (authorsWithTag.isNotEmpty()) {
                // Erstelle eine Menge von Autorennamen
                val authorNames = authorsWithTag.map { it.name }.toSet()
                // Alle Zitate abrufen und nach Autorennamen filtern
                val allQuotes = quoteDao.getAllQuotes()
                allQuotes.filter { it.authorName in authorNames }
            } else {
                emptyList() // Wenn keine Autoren gefunden werden
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Abrufen von Zitaten basierend auf dem Tag", e)
            emptyList() // Fehlerbehandlung
        }
    }


    // Liefert Zitate gefiltert nach Keywords
    suspend fun getQuotesByKeywords(keywords: List<String>): List<Quote> {
        return try {
            // Abrufen der gefilterten Zitate von der Mock API mit mehreren Keywords
            val filteredQuotes = MockApi.filterQuotesByKeywords(keywords)

            Log.d("QuoteRepository", "Filtered quotes by keywords '$keywords': $filteredQuotes")
            filteredQuotes // Rückgabe der gefilterten Zitate
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error filtering quotes by keywords '$keywords'", e)
            emptyList() // Rückgabe einer leeren Liste im Fehlerfall
        }
    }

    // Generiert ein Zitatbild basierend auf dem angegebenen Schlüsselwort
    suspend fun getImageByKeyword(keyword: String): Image? {
        return try {
            val image =
                MockApi.getImageByKeyword(keyword) // Bild basierend auf dem Schlüsselwort von der MockAPI abrufen
            Log.d(
                "QuoteRepository",
                "Mock API Response for image: $image"
            ) // Log für die MockAPI-Antwort
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

    // Ruft einen Autor anhand seines Namens ab, zuerst von der MockAPI, dann aus der Datenbank
    suspend fun getAuthorByName(authorName: String): Author? {
        return try {
            // Zuerst versuche, den Autor von der MockAPI abzurufen
            val apiAuthor = MockApi.getAuthorByName(authorName)
            if (apiAuthor != null) {
                // Optional: Speichere den Autor in der Datenbank, wenn er von der API abgerufen wurde
                quoteDao.insertAuthor(apiAuthor)
                return apiAuthor // Gib den Autor von der API zurück
            } else {
                // Wenn der Autor nicht von der API abgerufen werden konnte, versuche es in der lokalen Datenbank
                val author = quoteDao.getAuthorByName(authorName)
                return author // Gib den Autor aus der lokalen DB zurück, wenn gefunden
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error fetching author by name", e)
            null // Rückgabe von null im Fehlerfall
        }
    }


    // Funktion zum Abrufen der gespeicherten Zitate
    fun getSavedQuotes(): LiveData<List<Quote>> {
        return quoteDao.getAllSavedQuotes() // Diese Methode gibt bereits LiveData zurück
    }


    suspend fun updateQuote(quote: Quote) {
        withContext(Dispatchers.IO) {
            quoteDao.updateQuote(quote)
        }
    }

    // Fügt einen neuen Autor in die Datenbank ein und gibt zurück, ob das Einfügen erfolgreich war
    suspend fun addAuthor(author: Author): Boolean {
        return if (!author.name.isNullOrBlank()) {
            try {
                quoteDao.insertAuthor(author) // Autor in die Datenbank einfügen
                Log.d(
                    "QuoteRepository",
                    "Author added: $author"
                ) // Log für den erfolgreich hinzugefügten Autor
                true // Erfolgreiches Einfügen
            } catch (e: Exception) {
                Log.e(
                    "QuoteRepository",
                    "Error inserting author: ${e.message}",
                    e
                ) // Fehlermeldung mit spezifischem Fehler
                false // Fehler beim Einfügen
            }
        } else {
            Log.e(
                "QuoteRepository",
                "Author name is null or blank, not inserting."
            ) // Log für ungültigen Autor
            false // Ungültiger Autor
        }
    }













    // Speichert ein einzelnes Zitat in der lokalen Datenbank und gibt zurück, ob das Einfügen erfolgreich war
    private suspend fun insertQuote(quote: Quote): Boolean {
        return if (!quote.content.isNullOrBlank()) {
            try {
                quoteDao.insertQuote(quote) // Zitat in die Datenbank einfügen
                Log.d(
                    "QuoteRepository",
                    "Quote inserted successfully: ${quote.content}"
                ) // Log für erfolgreichen Insert
                true // Erfolgreiches Einfügen
            } catch (e: Exception) {
                Log.e(
                    "QuoteRepository",
                    "Error inserting quote: ${e.message}",
                    e
                ) // Fehlermeldung mit spezifischem Fehler
                false // Fehler beim Einfügen
            }
        } else {
            Log.e(
                "QuoteRepository",
                "Quote content is null or blank, not inserting."
            ) // Log für leeres Zitat
            false // Ungültiges Zitat
        }
    }


    // Speichert mehrere Zitate in der lokalen Datenbank und gibt zurück, wie viele erfolgreich gespeichert wurden
    suspend fun insertQuotes(quotes: List<Quote>): Int {
        return withContext(Dispatchers.IO) {
            try {
                // Filtere ungültige Zitate heraus (z.B. solche ohne Inhalt)
                val validQuotes =
                    quotes.filter { !it.content.isNullOrBlank() } // Prüfen auf null und leer

                if (validQuotes.isNotEmpty()) {
                    quoteDao.insertQuotes(validQuotes) // Gültige Zitate in die Datenbank einfügen
                    Log.d(
                        "QuoteRepository",
                        "Inserted ${validQuotes.size} valid quotes."
                    ) // Log für erfolgreiche Einfügung
                    validQuotes.size // Rückgabe der Anzahl erfolgreich eingefügter Zitate
                } else {
                    Log.e(
                        "QuoteRepository",
                        "No valid quotes to insert."
                    ) // Log, wenn keine gültigen Zitate vorhanden sind
                    0 // Keine gültigen Zitate, Rückgabe 0
                }
            } catch (e: Exception) {
                Log.e(
                    "QuoteRepository",
                    "Error inserting quotes: ${e.message}",
                    e
                ) // Fehlermeldung mit spezifischem Fehler
                0 // Rückgabe 0 im Fehlerfall
            }
        }
    }


    // Speichert mehrere Autoren in der lokalen Datenbank und gibt zurück, wie viele erfolgreich gespeichert wurden
    suspend fun insertAuthors(authors: List<Author>): Int {
        return withContext(Dispatchers.IO) {
            try {
                // Filtere ungültige Autoren heraus (z.B. solche ohne Namen)
                val validAuthors =
                    authors.filter { !it.name.isNullOrBlank() } // Prüfen auf null und leer

                if (validAuthors.isNotEmpty()) {
                    quoteDao.insertAuthors(validAuthors) // Gültige Autoren in die Datenbank einfügen
                    Log.d(
                        "QuoteRepository",
                        "Inserted ${validAuthors.size} valid authors."
                    ) // Log für erfolgreiche Einfügung
                    validAuthors.size // Rückgabe der Anzahl erfolgreich eingefügter Autoren
                } else {
                    Log.e(
                        "QuoteRepository",
                        "No valid authors to insert."
                    ) // Log, wenn keine gültigen Autoren vorhanden sind
                    0 // Keine gültigen Autoren, Rückgabe 0
                }
            } catch (e: Exception) {
                Log.e(
                    "QuoteRepository",
                    "Error inserting authors: ${e.message}",
                    e
                ) // Fehlermeldung mit spezifischem Fehler
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
                Log.w(
                    "QuoteRepository",
                    "No quote of the day found in local database."
                ) // Warnung, wenn kein Zitat gefunden wird
            }
            quote
        } catch (e: Exception) {
            Log.e(
                "QuoteRepository",
                "Error getting quote of the day",
                e
            ) // Fehlermeldung im Fehlerfall
            null
        }
    }

    // Ruft alle Zitate aus der lokalen Datenbank ab
    suspend fun getAllQuotesFromLocal(): List<Quote> {
        return try {
            val quotes = quoteDao.getAllQuotes() // Alle Zitate abrufen
            if (quotes.isEmpty()) {
                Log.w(
                    "QuoteRepository",
                    "No quotes found in local database."
                ) // Warnung, wenn keine Zitate gefunden werden
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
            Log.d(
                "QuoteRepository",
                "Quote deleted successfully: ${quote.content}"
            ) // Log für erfolgreichen Löschvorgang
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting quote", e) // Fehlermeldung im Fehlerfall
        }
    }

    // Ruft ein Zitat nach ID aus der lokalen Datenbank ab
    suspend fun getQuoteById(id: Int): Quote? {
        return try {
            val quote = quoteDao.getQuoteById(id) // Zitat nach ID abrufen
            if (quote != null) {
                Log.d(
                    "QuoteRepository",
                    "Quote retrieved successfully: ${quote.content}"
                ) // Log für erfolgreiches Abrufen
            } else {
                Log.e(
                    "QuoteRepository",
                    "No quote found for id: $id"
                ) // Log, wenn kein Zitat gefunden wurde
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
            Log.d(
                "QuoteRepository",
                "Author deleted successfully: ${author.name}, Rows deleted: $rowsDeleted"
            ) // Log für den Löschvorgang
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting author", e) // Fehlermeldung im Fehlerfall
        }
    }

    // Löscht alle Autoren aus der lokalen Datenbank
    suspend fun deleteAllAuthors() {
        try {
            val rowsDeleted = quoteDao.deleteAllAuthors() // Den Rückgabewert speichern
            Log.d(
                "QuoteRepository",
                "All authors deleted successfully, Rows deleted: $rowsDeleted"
            ) // Log für den Löschvorgang
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error deleting all authors", e) // Fehlermeldung im Fehlerfall
        }
    }

    suspend fun saveQuote(quote: Quote) {
        try {
            val updatedQuote = quote.copy(isSaved = true) // Setze isSaved auf true
            quoteDao.insertQuote(updatedQuote) // Speichere das aktualisierte Zitat in der Datenbank
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Fehler beim Speichern des Zitats: ${e.message}")
            throw e // Optional: Ausnahme weiterwerfen
        }
    }


}


