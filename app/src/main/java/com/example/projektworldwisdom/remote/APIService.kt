package com.example.projektworldwisdom.remote

import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote
import com.example.projektworldwisdom.model.Tag
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


const val BASE_URL = "https://api.quotable.io"


private val logger = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

private val client = okhttp3.OkHttpClient.Builder()
    .addInterceptor(logger)
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface WorldWisdomApiService {
    // Methoden für Zitate

    //https://api.quotable.io/random
    @GET("random")
    suspend fun getRandomQuote(): Quote

    //https://api.quotable.io/quotes/random
    @GET("quotes/random")
    suspend fun getMultipleRandomQuotes(): List<Quote>

    //https://api.quotable.io/quotes
    @GET("quotes")
    suspend fun getAllQuotes(): List<Quote>

    //https://api.quotable.io/search/quotes?query=life
    @GET("search/quotes")
    suspend fun searchQuotes(query: String): List<Quote>

    // Methoden für Autoren und Tags

    //https://api.quotable.io/authors
    @GET("authors")
    suspend fun getAllAuthors(): List<Author>

    //https://api.quotable.io/authors/random
    @GET("authors/random")
    suspend fun getRandomAuthor(): Author

    //https://api.quotable.io/search/authors?query=Albert
    @GET("search/authors")
    suspend fun searchAuthors(query: String): List<Author>

    //https://api.quotable.io/authors/123
    @GET("authors/{id}")
    suspend fun getAuthorById(@Path("id") id: String): Author

    //https://api.quotable.io/tags
    @GET("tags")
    suspend fun getAllTags(): List<Tag>

}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}