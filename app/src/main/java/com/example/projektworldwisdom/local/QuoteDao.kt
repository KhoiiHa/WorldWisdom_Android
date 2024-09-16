package com.example.projektworldwisdom.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    // Abrufen aller Zitate mit Limit
    @Query("SELECT * FROM quotes LIMIT :limit")
    suspend fun getAllQuotes(limit: Int): List<Quote>

    // Abrufen eines Zitats nach ID
    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Int): Quote?

    // Abrufen von Zitaten nach Schlüsselwort
    @Query("SELECT * FROM quotes WHERE content LIKE '%' || :keyword || '%'")
    suspend fun getQuotesByKeyword(keyword: String): List<Quote>

    // Abrufen des Zitats des Tages
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

    // Löschen eines Zitats
    @Delete
    suspend fun deleteQuote(quote: Quote)
}

