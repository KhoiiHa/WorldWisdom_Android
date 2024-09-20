package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "quotes",
    foreignKeys = [
        ForeignKey(
            entity = Author::class,
            parentColumns = ["id"],
            childColumns = ["authorName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Eindeutige ID für jedes Zitat
    @Json(name = "q") val content: String, // Zitattext
    @Json(name = "author") val authorName: String, // Verknüpfung mit dem Autorennamen
    @Json(name = "c") val characterCount: Int, // Zeichenanzahl
    @Json(name = "h") val html: String? = null, // vorformatiertes HTML-Zitat (optional)
    @Json(name = "i") val authorImageUrl: String? = null, // Neues Feld für Autorbild-URL
    val isQuoteOfTheDay: Boolean = false, // Neues Feld für das Zitat des Tages
    val isFavorite: Boolean = false // Neues Feld für Favoriten
)