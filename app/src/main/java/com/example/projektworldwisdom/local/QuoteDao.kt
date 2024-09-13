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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>)

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<Quote?>

    @Query("SELECT * FROM quotes WHERE _id = :id")
    suspend fun getQuoteById(id: String): Quote?

    @Query("SELECT * FROM quotes WHERE tags = :tag") // richtige tags muss noch hinzugefügt werden
    suspend fun getQuotesByTag(tag: String): List<Quote>

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): Quote?

    @Delete
    suspend fun deleteQuote(quote: Quote)


}

