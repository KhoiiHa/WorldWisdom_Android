package com.example.projektworldwisdom.model



import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "authors_table") // Tabellenname für die Room-Datenbank
data class Author(
    @PrimaryKey(autoGenerate = true) val id: Long? = null, // Primärschlüssel mit automatischer Generierung
    val name: String, // Name des Autors
    val tag: String, // Tag des Autors
    val link: String, // Link zu den Zitaten des Autors
    val biography: String // Biographie des Autors
)


