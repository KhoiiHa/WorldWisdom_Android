package com.example.projektworldwisdom.model

data class Quote(
    val _id: String,
    val content: String,
    val author: String,
    val tags: List<Tag>,
    val authorSlug: String,
)