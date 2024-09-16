package com.example.projektworldwisdom.remote

import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://zenquotes.io/api/"
private const val API_KEY = "70L87470TF537222S"

private val gson: Gson = GsonBuilder()
    .create()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create(gson))
    .baseUrl("$BASE_URL$API_KEY/")
    .client(client)
    .build()

interface WorldWisdomApiService {

    // Gibt mehrere zufällige Zitate zurück (auch einzelne Zitate)
    //Beispiel: https://zenquotes.io/api/quotes/random?count=5
    @GET("quotes/random")
    suspend fun getMultipleRandomQuotes(@Query("count") count: Int? = null): List<Quote>

    // Liefert eine Liste aller verfügbaren Zitate.
    // Beispiel: https://zenquotes.io/api/quotes?limit=10
    @GET("quotes")
    suspend fun getAllQuotes(@Query("limit") limit: Int = 10): List<Quote>

    // Liefert das Zitat des Tages.
    // Beispiel: https://zenquotes.io/api/today
    @GET("today")
    suspend fun getQuoteOfTheDay(): List<Quote>

    // Liefert eine Liste von Zitaten, die mit dem angegebenen Tag verknüpft sind.
    // Beispiel: https://zenquotes.io/api/quotes/tag/success
    @GET("quotes/")
    suspend fun searchQuotesByKeyword(@Query("keyword") keyword: String): List<Quote>

    // Liefert eine Liste aller verfügbaren Autoren.
    // Beispiel: https://zenquotes.io/api/authors
    @GET("authors")
    suspend fun getAuthors(): List<Author>

    // Liefert eine Liste aller verfügbaren Keywords.
    // Beispiel: https://zenquotes.io/api/keywords
    // Liefert eine Liste aller verfügbaren Keywords (mit API-Schlüssel als Query-Parameter für Premium-Version).
    @GET("keywords")
    suspend fun getKeywords(@Query("api_key") apiKey: String): List<String>
}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}