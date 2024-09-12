package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Author(
    @PrimaryKey(autoGenerate = true)
    val _id: String,
    val name: String,
    val bio: String?,
    val description: String?,
    val link: String,
    val quoteCount: Int?,
    val slug: String?
)
