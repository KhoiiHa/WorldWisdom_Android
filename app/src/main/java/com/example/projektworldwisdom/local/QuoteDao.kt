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
    suspend fun insertQuote(quote: Quote)  // Neue Methode für ein einzelnes Zitat

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthors(authors: List<Author>)

    @Query("SELECT * FROM authors_table WHERE id = :id")
    fun getAuthorById(id: Int): LiveData<Author>

    @Query("SELECT * FROM quote_table")
    fun getAllQuotes(): LiveData<List<Quote>>

    @Query("SELECT * FROM authors_table")
    fun getAllAuthors(): LiveData<List<Author>>

    @Query("SELECT * FROM quote_table WHERE id = :id")
    fun getQuoteById(id: Int): LiveData<Quote>

    @Query("DELETE FROM quote_table WHERE id = :id")
    suspend fun deleteQuoteById(id: Int)

    @Query("SELECT * FROM quote_table WHERE isQuoteOfTheDay = 1")
    fun getQuoteOfTheDay(): LiveData<Quote>

    @Query("SELECT * FROM quote_table WHERE isSaved = 1")
    fun getSavedQuotes(): LiveData<List<Quote>>

    @Query("SELECT * FROM quote_table WHERE author LIKE :authorName")
    fun getQuotesByAuthorName(authorName: String): LiveData<List<Quote>>

    @Query("SELECT * FROM quote_table WHERE content LIKE :keyword")
    fun searchQuotesByKeyword(keyword: String): LiveData<List<Quote>>

    @Query("SELECT * FROM quote_table WHERE keywords LIKE '%' || :keyword || '%'")
    fun getQuotesByKeyword(keyword: String): LiveData<List<Quote>>

    @Update
    suspend fun updateQuote(quote: Quote)
}