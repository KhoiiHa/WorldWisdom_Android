package com.example.projektworldwisdom.converters

import androidx.room.TypeConverter
import com.example.projektworldwisdom.model.Author
import com.google.gson.Gson


class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAuthor(author: Author): String {
        return gson.toJson(author) // Konvertiere Author-Objekt in JSON-String
    }

    @TypeConverter
    fun toAuthor(authorString: String): Author {
        return gson.fromJson(authorString, Author::class.java) // Konvertiere JSON-String zurück in Author-Objekt
    }
}