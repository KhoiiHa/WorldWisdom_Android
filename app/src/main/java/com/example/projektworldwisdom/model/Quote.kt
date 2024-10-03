package com.example.projektworldwisdom.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projektworldwisdom.converters.Converters
import kotlinx.parcelize.Parcelize

@Entity(tableName = "quote_table")
@TypeConverters(Converters::class)
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID wird automatisch generiert
    val content: String, // Inhalt des Zitats
    val author: List<Author>, // Name des Autors
    val isQuoteOfTheDay: Boolean, // Ob es sich um das Zitat des Tages handelt
    val isSaved: Boolean, // Ob das Zitat gespeichert ist
    val keywords: String, // Schlüsselwörter für die Suche
    val comments: String // Kommentare zum Zitat
)


