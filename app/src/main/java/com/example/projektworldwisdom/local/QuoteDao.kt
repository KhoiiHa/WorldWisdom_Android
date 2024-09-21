package com.example.projektworldwisdom.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Note
import com.example.projektworldwisdom.model.Quote

@Dao
interface QuoteDao {

    // Fügt ein einzelnes Zitat in die Datenbank ein oder aktualisiert es, falls es bereits existiert.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)

    // Fügt mehrere Zitate in die Datenbank ein oder aktualisiert sie, falls sie bereits existieren.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>)

    // Fügt eine Liste von Autoren in die Datenbank ein oder aktualisiert sie, falls sie bereits existieren.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthors(authors: List<Author>)

    // Fügt einen einzelnen Autor in die Datenbank ein oder aktualisiert ihn, falls er bereits existiert.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(author: Author)

    // Ruft alle Zitate aus der Datenbank ab, die mit dem angegebenen Autorennamen verknüpft sind.
    @Query("SELECT * FROM quotes WHERE authorName = :authorName")
    suspend fun getQuotesByAuthor(authorName: String): List<Quote>

    // Ruft alle Autoren aus der lokalen Datenbank ab
    @Query("SELECT * FROM authors_table")
    suspend fun getAllAuthors(): List<Author>

    // Abrufen eines Autors nach Name (sichergestellt, dass das Feld korrekt ist)
    @Query("SELECT * FROM authors_table WHERE name = :authorName")
    suspend fun getAuthorByName(authorName: String): Author?

    // Abrufen von Zitaten eines bestimmten Autors aus der lokalen Datenbank
    @Query("SELECT * FROM quotes WHERE authorName = :authorName")
    suspend fun searchQuotesByAuthor(authorName: String): List<Quote>

    // Löschen aller Autoren aus der lokalen Datenbank
    @Query("DELETE FROM authors_table")
    suspend fun deleteAllAuthors()

    // Abrufen aller Zitate aus der lokalen Datenbank.
    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<Quote>

    // Abrufen eines Zitats aus der lokalen Datenbank anhand der angegebenen ID.
    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): Quote?

    // Abrufen von Zitaten aus der lokalen Datenbank, die das angegebene Schlüsselwort im Inhalt enthalten.
    @Query("SELECT * FROM quotes WHERE content LIKE '%' || :keyword || '%'")
    suspend fun getQuotesByKeyword(keyword: String): List<Quote>

    // Abrufen des Zitats des Tages aus der lokalen Datenbank, das als solches markiert ist.
    @Query("SELECT * FROM quotes WHERE isQuoteOfTheDay = 1 LIMIT 1")
    suspend fun getQuoteOfTheDay(): Quote?

    // Abrufen mehrerer zufälliger Zitate aus der lokalen Datenbank, begrenzt durch die angegebene Anzahl.
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT :count")
    suspend fun getMultipleRandomQuotes(count: Int): List<Quote>

    // Abrufen eines zufälligen Zitats aus der lokalen Datenbank.
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): Quote?

    // Abrufen aller Favoriten-Zitate aus der lokalen Datenbank.
    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoriteQuotes(): List<Quote>

    // Ruft alle Schlüsselwörter aus der lokalen Datenbank ab
    @Query("SELECT * FROM keywords_table")
    suspend fun getAllKeywords(): List<Keyword>

    // Löschen eines bestimmten Zitats aus der lokalen Datenbank.
    @Delete
    suspend fun deleteQuote(quote: Quote)

    // Löschen eines bestimmten Autors aus der lokalen Datenbank.
    @Delete
    suspend fun deleteAuthor(author: Author)
}

