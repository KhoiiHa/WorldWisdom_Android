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
) : Parcelable


