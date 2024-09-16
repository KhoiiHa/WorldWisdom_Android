package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Eindeutige ID für jedes Zitat
    @Json(name = "q") val content: String, // Zitattext
    @Json(name = "a") val author: String, // Autorname
    @Json(name = "c") val characterCount: Int, // Zeichenanzahl
    @Json(name = "h") val html: String? = null, // vorformatiertes HTML-Zitat (optional)
    val isQuoteOfTheDay: Boolean = false, // Neues Feld für das Zitat des Tages
    val isFavorite: Boolean = false // Neues Feld für Favoriten
)