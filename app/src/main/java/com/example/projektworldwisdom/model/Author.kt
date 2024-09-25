package com.example.projektworldwisdom.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "authors") // Tabellenname für die Room-Datenbank
data class Author(
    @PrimaryKey val id: Int = 0, // Eindeutige ID für jeden Autor
    val name: String, // Name des Autors
    val tag: String, // Tag des Autors
    val link: String, // Link zu den Zitaten des Autors
    val imageUrl: String? = null // Bild-URL des Autors (optional)
): Parcelable

