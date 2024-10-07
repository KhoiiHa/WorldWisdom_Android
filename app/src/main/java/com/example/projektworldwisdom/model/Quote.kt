package com.example.projektworldwisdom.model



import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "quote_table")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID wird automatisch generiert
    val content: String, // Inhalt des Zitats
    val author: Author, // Autor des Zitats (Objekt der Author-Klasse)
    var isQuoteOfTheDay: Boolean, // Ob es sich um das Zitat des Tages handelt
    var isSaved: Boolean, // Ob das Zitat gespeichert ist
    val keywords: String, // Schlüsselwörter für die Suche
    val comments: String? = null // Kommentare zum Zitat
)


