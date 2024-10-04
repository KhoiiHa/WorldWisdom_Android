package com.example.projektworldwisdom.converters

import androidx.room.TypeConverter
import com.example.projektworldwisdom.model.Author
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // Konvertiere List<Author> in einen JSON-String
    @TypeConverter
    fun fromAuthorList(authors: List<Author>?): String? {
        return gson.toJson(authors)
    }

    // Konvertiere einen JSON-String in eine List<Author>
    @TypeConverter
    fun toAuthorList(authorsString: String?): List<Author>? {
        val listType = object : TypeToken<List<Author>>() {}.type
        return gson.fromJson(authorsString, listType)
    }

    // Optional: Deine bestehenden Konverter für String-Listen
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",") ?: emptyList()
    }

    @TypeConverter
    fun listToString(list: List<String>): String {
        return list.joinToString(",")
    }
}