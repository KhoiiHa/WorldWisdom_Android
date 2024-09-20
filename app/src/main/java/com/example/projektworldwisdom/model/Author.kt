package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "authors_table")
data class Author(
    @PrimaryKey
    val id: Int = 0,
    @Json(name = "a") val name: String, // Name des Autors
    @Json(name = "t") val tag: String, // Tag des Autors
    @Json(name = "l") val link: String, // Link zu den Zitaten des Autors
    val quoteCount: Int, // Anzahl der Zitate des Autors
    val imageUrl: String? = null // Bild-URL des Autors (optional)
)

