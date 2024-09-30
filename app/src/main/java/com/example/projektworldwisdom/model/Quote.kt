package com.example.projektworldwisdom.model


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projektworldwisdom.converters.Converters
import kotlinx.parcelize.Parcelize

@Entity(tableName = "quotes")
@TypeConverters(Converters::class)
@Parcelize
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String?,
    val authorName: String?,
    val authorImageUrl: String? = null,
    val isQuoteOfTheDay: Boolean = false,
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,
    val keywords: List<String> = emptyList(),
    val comments: List<String> = emptyList()
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Quote) return false

        return id == other.id &&
                content == other.content &&
                authorName == other.authorName &&
                keywords == other.keywords &&
                comments == other.comments
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (content?.hashCode() ?: 0)
        result = 31 * result + (authorName?.hashCode() ?: 0)
        result = 31 * result + keywords.hashCode()
        result = 31 * result + comments.hashCode()
        return result
    }
}