package com.example.projektworldwisdom.model

data class AuthorSearchResult(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int?,
    val results: List<Author>
)