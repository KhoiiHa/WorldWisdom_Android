package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val _id: String,
    val name: String,
    val slug: String,
    val quoteCount: Int,
    val dateAdded: String,
    val dateModified: String
)