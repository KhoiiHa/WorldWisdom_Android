package com.example.projektworldwisdom.model

data class Quote(
    val id: String,
    val author: String,
    val quote: String,
    val category: String,
    val tags: List<String>,
    val isFavorite: Boolean,
    val description: String,
    val source: String,
    val authorImageURLs: List<String>
) {
    val authorSlug: String
        get() = author
            .lowercase()
            .replace(" ", "-")
            .replace(".", "")
            .replace(",", "")
            .replace("ö", "oe")
            .replace("ä", "ae")
            .replace("ü", "ue")
}