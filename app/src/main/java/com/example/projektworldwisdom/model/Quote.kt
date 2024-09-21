package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Eindeutige ID für jedes Zitat
    @Json(name = "q") val content: String, // Zitattext
    @Json(name = "a") val authorName: String, // Autorname
    @Json(name = "i") val authorImageUrl: String? = null, // Bild-URL des Autors (optional)
    @Json(name = "h") val html: String? = null, // vorformatiertes HTML-Zitat (optional)
    val isQuoteOfTheDay: Boolean = false, // Zitat des Tages
    val isFavorite: Boolean = false // Favoriten-Zitat
)