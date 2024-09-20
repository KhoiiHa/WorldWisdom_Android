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

    // Liefert eine Liste von 50 zufälligen Zitaten
    @GET("quotes")
    suspend fun getAllQuotes(): List<Quote>

    // Liefert das Zitat des Tages
    @GET("today")
    suspend fun getQuoteOfTheDay(): Quote

    // Liefert eine Liste von Zitaten, die mit dem angegebenen Tag verknüpft sind.
    @GET("quotes/tag/{keyword}")
    suspend fun searchQuotesByKeyword(
        @Path("keyword") keyword: String,
        @Query("api_key") apiKey: String = API_KEY
    ): List<Quote>

    // Liefert eine Liste von Zitaten eines bestimmten Autors.
    @GET("quotes/author/{authorName}")
    suspend fun getQuotesByAuthor(
        @Path("authorName") authorName: String,
        @Query("api_key") apiKey: String = API_KEY
    ): List<Quote>

    // Liefert eine Liste aller verfügbaren Autoren.
    @GET("authors")
    suspend fun getAuthors(
        @Query("api_key") apiKey: String = API_KEY
    ): List<Author>

    // Generiert ein Zitat-Bild eines bestimmten Autors.
    @GET("image/author/{authorName}")
    suspend fun getQuoteImageByAuthor(
        @Path("authorName") authorName: String,
        @Query("api_key") apiKey: String = API_KEY
    ): Image

    // Generiert ein Zitatbild basierend auf unterstützten Keywords.
    @GET("image")
    suspend fun getImageByKeyword(
        @Query("keyword") keyword: String,
        @Query("api_key") apiKey: String = API_KEY
    ): List<Image>

    // Liefert eine Liste aller verfügbaren Keywords.
    @GET("keywords")
    suspend fun getKeywords(
        @Query("api_key") apiKey: String = API_KEY
    ): List<String>
}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}