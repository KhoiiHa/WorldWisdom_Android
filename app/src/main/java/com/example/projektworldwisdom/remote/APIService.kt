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
import retrofit2.http.Url

private const val BASE_URL = "https://zenquotes.io/api/"
private const val API_KEY = "787963bc6b2630495d3d9f25bba4a331"

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
    .baseUrl("http://localhost/") // Basis-URL der Mock-API
    .client(client)
    .build()

interface WorldWisdomApiService {

    // Liefert eine Liste von 50 zufälligen Zitaten (kein API-Key erforderlich)
    //https://zenquotes.io/api/quotes
    @GET("quotes")
    suspend fun getAllQuotes(): List<Quote>

    // Liefert das Zitat des Tages (kein API-Key erforderlich)
    //https://zenquotes.io/api/today
    @GET("today")
    suspend fun getQuoteOfTheDay(): List<Quote>

    // Liefert ein zufälliges Zitat (kein API-Key erforderlich)
    //https://zenquotes.io/api/random
    @GET("random")
    suspend fun getRandomQuote(): Quote

    // Liefert ein zufälliges inspirierendes Bild (kein API-Key erforderlich)
    //https://zenquotes.io/api/image
    @GET("image")
    suspend fun getRandomInspirationalImage(): Image

    // Liefert eine Liste aller verfügbaren Autoren.
    // https://zenquotes.io/api/authors?api_key=YOUR_KEY
    @GET("authors")
    suspend fun getAuthors(@Query("api_key") apiKey: String = API_KEY): List<Author>

    // Liefert eine Liste von Zitaten eines bestimmten Autors.
    // https://zenquotes.io/api/quotes/author/sun-tzu?api_key=YOUR_KEY
    @GET("quotes/author/{authorName}")
    suspend fun getQuotesByAuthor(
        @Path("authorName") authorName: String,
        @Query("api_key") apiKey: String = API_KEY
    ): List<Quote>

    // Liefert ein Zitat-Bild eines bestimmten Autors.
    // https://zenquotes.io/api/image/author/sun-tzu?api_key=YOUR_KEY
    @GET("image/author/{authorName}")
    suspend fun getQuoteImageByAuthor(
        @Path("authorName") authorName: String,
        @Query("api_key") apiKey: String = API_KEY
    ): Image

    // Filtert Zitate nach unterstützten Schlüsselwörtern.
    // https://zenquotes.io/api/quotes?api_key=YOUR_KEY&keyword=change
    @GET("quotes")
    suspend fun filterQuotesByKeyword(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("keyword") keyword: String
    ): List<Quote>

    // Generiert ein Zitatbild basierend auf unterstützten Keywords.
    // https://zenquotes.io/api/image?api_key=YOUR_KEY&keyword=change
    @GET("image")
    suspend fun getImageByKeyword(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("keyword") keyword: String
    ): Image


}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}