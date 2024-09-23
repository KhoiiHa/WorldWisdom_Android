package com.example.projektworldwisdom.model


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projektworldwisdom.converters.Converters

@Entity(tableName = "quotes") // Tabelle "quotes" in der Datenbank
@TypeConverters(Converters::class)
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Eindeutige ID für jedes Zitat
    val content: String?, // Zitattext
    val authorName: String?, // Autorname
    val authorImageUrl: String? = null, // Bild-URL des Autors (optional)
    val isQuoteOfTheDay: Boolean = false, // Zitat des Tages
    val isFavorite: Boolean = false, // Favoriten-Zitat
    val keywords: List<String> = emptyList() // Neue Liste für Keywords
)