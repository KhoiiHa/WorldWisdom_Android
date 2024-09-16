package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Keyword(
    @PrimaryKey
    val keyword: String
)

