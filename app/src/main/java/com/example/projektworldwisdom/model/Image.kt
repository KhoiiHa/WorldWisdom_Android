package com.example.projektworldwisdom.model

import com.squareup.moshi.Json

data class Image(
    @Json(name = "i") val imageUrl: String? // Bild-URL des Zitats
)