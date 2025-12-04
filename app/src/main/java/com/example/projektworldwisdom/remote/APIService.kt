package com.example.projektworldwisdom.remote

import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.AuthorSearchResult
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

private const val BASE_URL = "http://10.0.2.2:3002/"

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
    // Gibt alle Zitate aus dem lokalen Mockoon-Backend zurück
    @GET("api/quotes")
    suspend fun getAllQuotes(): List<Quote>
}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}