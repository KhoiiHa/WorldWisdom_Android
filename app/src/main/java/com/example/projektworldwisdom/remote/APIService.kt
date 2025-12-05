package com.example.projektworldwisdom.remote

import com.example.projektworldwisdom.model.Quote
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

// 🔗 WICHTIG: Emulator → Localhost = 10.0.2.2
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
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .client(client)
    .build()

interface WorldWisdomApiService {
    // Holt alle Zitate aus Mockoon
    @GET("api/quotes")
    suspend fun getAllQuotes(): List<Quote>
}

object WorldWisdomApi {
    val retrofitService: WorldWisdomApiService by lazy {
        retrofit.create(WorldWisdomApiService::class.java)
    }
}