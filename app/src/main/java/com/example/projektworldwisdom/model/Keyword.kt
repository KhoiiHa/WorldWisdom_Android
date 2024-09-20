package com.example.projektworldwisdom.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keywords_table")
data class Keyword(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String // Hier speicherst du das Schlüsselwort
)

