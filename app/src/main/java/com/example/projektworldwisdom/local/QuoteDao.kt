package com.example.projektworldwisdom.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)  // Ein einzelnes Zitat einfügen

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>) // Mehrere Zitate einfügen

    // Abfrage für Zitate eines bestimmten Autors
    @Query("SELECT * FROM quote_table WHERE author = :authorName")
    suspend fun getQuotesByAuthorName(authorName: String): List<Quote>

    @Query("SELECT * FROM authors_table WHERE name = :authorName LIMIT 1")
    suspend fun getAuthorByName(authorName: String): Author?

    @Query("SELECT * FROM quote_table")
    fun getAllQuotes(): LiveData<List<Quote>> // Alle Zitate abrufen

    @Query("SELECT * FROM quote_table WHERE id = :id")
    fun getQuoteById(id: Int): LiveData<Quote> // Zitat nach ID abrufen

    @Query("DELETE FROM quote_table WHERE id = :id")
    suspend fun deleteQuoteById(id: Int) // Zitat nach ID löschen

    @Query("SELECT * FROM quote_table WHERE isQuoteOfTheDay = 1 LIMIT 1")
    fun getQuoteOfTheDay(): LiveData<Quote> // Nur ein Zitat des Tages abrufen

    @Query("UPDATE quote_table SET isQuoteOfTheDay = 0") // Setze alle Zitate auf nicht Zitat des Tages
    suspend fun resetAllQuotesOfTheDay()

    @Query("SELECT * FROM quote_table WHERE isSaved = 1")
    fun getSavedQuotes(): LiveData<List<Quote>> // Gespeicherte Zitate abrufen

    @Query("SELECT * FROM quote_table WHERE content LIKE :keyword")
    fun searchQuotesByKeyword(keyword: String): LiveData<List<Quote>> // Zitate nach Schlüsselwort suchen

    @Query("SELECT * FROM quote_table WHERE keywords LIKE '%' || :keyword || '%'")
    fun getQuotesByKeyword(keyword: String): LiveData<List<Quote>> // Zitate anhand eines Stichworts abrufen

    @Update
    suspend fun updateQuote(quote: Quote) // Zitat aktualisieren
}