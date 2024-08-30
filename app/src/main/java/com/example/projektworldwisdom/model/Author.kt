package com.example.projektworldwisdom.model

data class Author(
    val _id: String,
    val name: String,
    val bio: String,
    val description: String,
    val link: String,
    val quoteCount: Int,
    val slug: String
)
