package com.example.projektworldwisdom.local

import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.remote.WorldWisdomApiService

class QuoteRepository(private val quoteDao: QuoteDao, private val apiService: WorldWisdomApiService) {

    suspend fun getAllQuotes(): List<Quote?> {
        return try {
            val quotes = apiService.getAllQuotes()
            quoteDao.insertQuotes(quotes)
            quotes
        } catch (e: Exception) {
            quoteDao.getAllQuotes()
        }
    }

    suspend fun getQuoteById(id: String): Quote? {
        return quoteDao.getQuoteById(id)
    }

    suspend fun insertQuote(quote: Quote) {
        quoteDao.insertQuote(quote)
    }

    suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(quote)
    }

    // Methode für das Laden eines zufälligen Zitats
    suspend fun getRandomQuote(): Quote? {
        return try {
            // Versuche, ein zufälliges Zitat von der API zu bekommen
            apiService.getRandomQuote()
        } catch (e: Exception) {
            // Fallback: Lade ein zufälliges Zitat aus der lokalen Datenbank
            quoteDao.getRandomQuote()
        }
    }

    // Methode für das Suchen von Zitaten nach Tag
    suspend fun searchQuotesByTag(tag: String): List<Quote> {
        return try {
            val result = apiService.searchQuotes(tag)
            result.results
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Methode zum Einfügen von mehreren Zitaten in die Datenbank
    suspend fun insertQuotes(quotes: List<Quote>) {
        quoteDao.insertQuotes(quotes)
    }

    // Methode zum Abrufen von Zitaten nach Tag aus der lokalen Datenbank
    suspend fun getQuotesByTag(tag: String): List<Quote> {
        return quoteDao.getQuotesByTag(tag)
    }

    // Methode zum Abrufen eines zufälligen Zitats aus der lokalen Datenbank
    suspend fun getRandomLocalQuote(): Quote? {
        return try {
            quoteDao.getRandomQuote()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getQuotesByTagFromLocal(tag: String): List<Quote> {
        return quoteDao.getQuotesByTag(tag)
    }


    suspend fun getAllLocalQuotes(): List<Quote> {
        return quoteDao.getAllQuotes().filterNotNull()
    }
}