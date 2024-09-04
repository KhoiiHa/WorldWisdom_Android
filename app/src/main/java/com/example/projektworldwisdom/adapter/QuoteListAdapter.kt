package com.example.projektworldwisdom.adapter

import android.util.JsonToken
import android.util.JsonReader
import android.util.JsonWriter
import com.example.projektworldwisdom.model.Quote
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

 class QuoteListAdapter : JsonAdapter<List<Quote>>() {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val quoteAdapter = moshi.adapter(Quote::class.java)

     override fun fromJson(reader: JsonReader): List<Quote>? {
         return when (val peeked = reader.peek()) { // Speichere das Ergebnis von peek() in einer Variable
             JsonReader.Token.BEGIN_ARRAY -> {
                 reader.beginArray()
                 val quotes = mutableListOf<Quote>()
                 while (reader.hasNext()) {
                     quoteAdapter.fromJson(reader)?.let { quotes.add(it) }
                 }
                 reader.endArray()
                 quotes
             }
             JsonReader.Token.BEGIN_OBJECT -> {
                 listOfNotNull(quoteAdapter.fromJson(reader))
             }
             null -> { // Behandle den Fall, dass peek() null zurückgibt
                 null // Oder eine andere geeignete Fehlerbehandlung
             }
             else -> { // Behandle andere unerwartete Token-Typen
                 reader.skipValue()
                 null // Oder eine andere geeignete Fehlerbehandlung
             }
         }
     }

    override fun toJson(writer: JsonWriter, value: List<Quote>?) {
        throw UnsupportedOperationException("Not implemented")
    }
}