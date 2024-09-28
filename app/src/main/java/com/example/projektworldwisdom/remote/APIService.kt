package com.example.projektworldwisdom.remote

import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Image
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

private const val BASE_URL = "http://localhost/" // Basis-URL der Mock-API

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
    .baseUrl(BASE_URL) // Verwende die Basis-URL der Mock-API
    .client(client)
    .build()

interface WorldWisdomApiService {

    // Liefert eine Liste von Zitaten (Mock-API)
    @GET("mock/quotes") // Beispiel-Pfad für Mockdaten
    suspend fun getAllQuotes(): List<Quote>

    // Liefert das Zitat des Tages (Mock-API)
    @GET("mock/today") // Beispiel-Pfad für Mockdaten
    suspend fun getQuoteOfTheDay(): List<Quote>

    // Liefert ein zufälliges Zitat (Mock-API)
    @GET("mock/random") // Beispiel-Pfad für Mockdaten
    suspend fun getRandomQuote(): Quote

    // Liefert eine Liste aller verfügbaren Autoren (Mock-API)
    @GET("mock/authors") // Beispiel-Pfad für Mockdaten
    suspend fun getAuthors(): List<Author>

    // Liefert eine Liste von Zitaten eines bestimmten Autors (Mock-API)
    @GET("mock/quotes/author/{authorName}") // Beispiel-Pfad für Mockdaten
    suspend fun getQuotesByAuthor(
        @Path("authorName") authorName: String
    ): List<Quote>

    // Liefert ein Zitat-Bild eines bestimmten Autors (Mock-API)
    @GET("mock/image/author/{authorName}") // Beispiel-Pfad für Mockdaten
    suspend fun getQuoteImageByAuthor(
        @Path("authorName") authorName: String
    ): Image

    // Filtert Zitate nach unterstützten Schlüsselwörtern (Mock-API)
    @GET("mock/quotes") // Beispiel-Pfad für Mockdaten
    suspend fun filterQuotesByKeyword(
        @Query("keyword") keyword: String
    ): List<Quote>

    // Generiert ein Zitatbild basierend auf unterstützten Keywords (Mock-API)
    @GET("mock/image") // Beispiel-Pfad für Mockdaten
    suspend fun getImageByKeyword(
        @Query("keyword") keyword: String
    ): Image
}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}