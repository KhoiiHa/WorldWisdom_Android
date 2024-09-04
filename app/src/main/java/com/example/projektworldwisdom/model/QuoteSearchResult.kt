package com.example.projektworldwisdom.model

data class QuoteSearchResult(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val results: List<Quote>
)
