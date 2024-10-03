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

private const val BASE_URL = "https://66fbfc918583ac93b40e1f55.mockapi.io/"

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
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface WorldWisdomApiService {

    // Liefert eine Liste von Zitaten (Mock-API)
    @GET("quotes")
    suspend fun getAllQuotes(): List<Quote>

    // Liefert eine Liste von Autoren
    @GET("authors")
    suspend fun getAllAuthors(): List<Author>


}
object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy { retrofit.create(WorldWisdomApiService::class.java) }
}