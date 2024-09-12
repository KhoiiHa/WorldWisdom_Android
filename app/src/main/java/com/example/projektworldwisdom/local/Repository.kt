package com.example.projektworldwisdom.local

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    private var instance: QuoteDatabase? = null

    fun getDatabase(context: Context): QuoteDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                QuoteDatabase::class.java,
                "quote_database"
            ).build()
        }
        return instance!!
    }
}