package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class QuoteSearchResult(
    @PrimaryKey(autoGenerate = true)
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val results: List<Quote>
)
