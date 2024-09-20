package com.example.projektworldwisdom.model

import com.squareup.moshi.Json

data class Image(
    @Json(name = "q") val quote: String, // Zitattext
    @Json(name = "a") val author: String, // Autorname
    @Json(name = "h") val html: String // vorformatiertes HTML-Zitat
)