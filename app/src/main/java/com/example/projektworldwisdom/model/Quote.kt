package com.example.projektworldwisdom.model



import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "quote_table")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID wird automatisch generiert
    val content: String, // Inhalt des Zitats
    val author: List<Int>, // Name des Autors //erstzen mit Author
    val isQuoteOfTheDay: Boolean, // Ob es sich um das Zitat des Tages handelt
    val isSaved: Boolean, // Ob das Zitat gespeichert ist
    val keywords: String, // Schlüsselwörter für die Suche
    val comments: String? = null // Kommentare zum Zitat
)


