package com.example.projektworldwisdom.remote

import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.model.QuoteSearchResult
import com.example.projektworldwisdom.model.SingleQuoteResponse
import com.example.projektworldwisdom.model.Tag
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.squareup.moshi.Types

private const val BASE_URL = "https://api.quotable.io"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface WorldWisdomApiService {
    // Methoden für Zitate

    //https://api.quotable.io/random
    // Gibt ein zufälliges Zitat zurück
//    @GET("random")
//    suspend fun getRandomQuote(): SingleQuoteResponse

    //https://api.quotable.io/quotes/random
    // Gibt mehrere zufällige Zitate zurück
    @GET("quotes/random")
    suspend fun getMultipleRandomQuotes(@Query("count") count: Int = 1): QuoteSearchResult

    //https://api.quotable.io/quotes
    // Gibt alle Zitate zurück
    @GET("quotes")
    suspend fun getAllQuotes(): List<Quote>

    //https://api.quotable.io/search/quotes?query=life
    // Suche nach Zitaten basierend auf einem Suchbegriff
    @GET("search/quotes")
    suspend fun searchQuotes(@Query("query") query: String): QuoteSearchResult

    // Methoden für Autoren und Tags

    //https://api.quotable.io/authors
    // Gibt alle Autoren zurück
    @GET("authors")
    suspend fun getAllAuthors(): List<Author>

    //https://api.quotable.io/authors/random
    // Gibt einen zufälligen Autor zurück
    @GET("authors/random")
    suspend fun getRandomAuthor(): Author

    //https://api.quotable.io/search/authors?query=Albert
    // Suche nach Autoren basierend auf einem Suchbegriff
    @GET("search/authors")
    suspend fun searchAuthors(@Query("query") query: String): List<Author>

    //https://api.quotable.io/authors/123
    // Gibt einen Autor anhand seiner ID zurück
    @GET("authors/{id}")
    suspend fun getAuthorById(@Path("id") id: String): Author

    //https://api.quotable.io/tags
    // Gibt alle Tags zurück
    @GET("tags")
    suspend fun getAllTags(): List<Tag>

}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}