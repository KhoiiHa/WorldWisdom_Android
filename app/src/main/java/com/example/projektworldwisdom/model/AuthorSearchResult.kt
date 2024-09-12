package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AuthorSearchResult(
    @PrimaryKey(autoGenerate = true)
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int?,
    val results: List<Author>
)