package com.example.projektworldwisdom.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projektworldwisdom.converters.Converters
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Note
import com.example.projektworldwisdom.model.Quote

@Database(entities = [Quote::class, Note::class, Author::class, Keyword::class], version = 2)
@TypeConverters(Converters::class)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null

        fun getDatabase(context: Context): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quote_database"
                )
                    .fallbackToDestructiveMigration() // Datenbank wird bei Schemaänderungen gelöscht
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}