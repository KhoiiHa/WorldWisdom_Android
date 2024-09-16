package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity
data class Author(
    @PrimaryKey
    val id: Int = 0,
    @Json(name = "a") val name: String,
    @Json(name = "t") val tag: String,
    @Json(name = "l") val link: String,
    val quoteCount: Int // quoteCount als Int
)

