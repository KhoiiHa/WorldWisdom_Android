package com.example.projektworldwisdom.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "authors") // Tabellenname für die Room-Datenbank
data class Author(
    @PrimaryKey val id: Int = 0, // Eindeutige ID für jeden Autor
    val name: String, // Name des Autors
    val tag: String, // Tag des Autors
    val link: String?, // Link zu den Zitaten des Autors
    val imageUrl: String? = null // Bild-URL des Autors (optional)
) : Parcelable {

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + tag.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + (imageUrl?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Author) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (tag != other.tag) return false
        if (link != other.link) return false
        if (imageUrl != other.imageUrl) return false

        return true
    }
}
