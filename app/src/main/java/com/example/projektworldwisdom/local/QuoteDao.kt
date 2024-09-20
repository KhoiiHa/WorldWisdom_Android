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

    // Einfügen eines einzelnen Zitats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)

    // Einfügen mehrerer Zitate
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>)

    // Fügt eine Liste von Autoren in die lokale Datenbank ein
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthors(authors: List<Author>)

    // Einfügen eines einzelnen Autors
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(author: Author)

    // Abrufen von Zitat
    @Query("SELECT * FROM quotes WHERE authorName = :authorName")
    suspend fun getQuotesByAuthor(authorName: String): List<Quote>

    // Ruft alle Autoren aus der lokalen Datenbank ab
    @Query("SELECT * FROM authors_table")
    fun getAllAuthors(): List<Author>

    // Abrufen eines Autors nach Name
    @Query("SELECT * FROM authors_table WHERE name = :authorName")
    suspend fun getAuthorByName(authorName: String): Author?

    // Abrufen von Zitaten eines bestimmten Autors aus der lokalen Datenbank
    @Query("SELECT * FROM quotes WHERE authorName = :authorName")
    suspend fun searchQuotesByAuthor(authorName: String): List<Quote>

    // Löscht alle Autoren aus der lokalen Datenbank
    @Query("DELETE FROM authors_table")
    suspend fun deleteAllAuthors()

    // Abrufen aller Zitate ohne Limit
    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<Quote>

    // Abrufen eines Zitats nach ID
    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): Quote?


    // Abrufen von Zitaten nach Schlüsselwort
    @Query("SELECT * FROM quotes WHERE content LIKE '%' || :keyword || '%'")
    suspend fun getQuotesByKeyword(keyword: String): List<Quote>

    // Abrufen des Zitats des Tages aus der lokalen Datenbank
    @Query("SELECT * FROM quotes WHERE isQuoteOfTheDay = 1 LIMIT 1")
    suspend fun getQuoteOfTheDay(): Quote?

    // Abrufen mehrerer zufälliger Zitate
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT :count")
    suspend fun getMultipleRandomQuotes(count: Int): List<Quote>

    // Abrufen eines zufälligen Zitats
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): Quote?

    // Abrufen von Favoriten-Zitaten
    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoriteQuotes(): List<Quote>

    // Ruft alle Schlüsselwörter aus der lokalen Datenbank ab
    @Query("SELECT * FROM keywords_table") // Passe den Tabellennamen an, falls nötig
    suspend fun getAllKeywords(): List<Keyword>

    // Löschen eines Zitats
    @Delete
    suspend fun deleteQuote(quote: Quote)

    // Löscht einen bestimmten Autor aus der lokalen Datenbank
    @Delete
    suspend fun deleteAuthor(author: Author)


}

