package com.example.projektworldwisdom.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projektworldwisdom.model.Note
import com.example.projektworldwisdom.model.Quote



@Database(entities = [Quote::class, Note::class], version = 3) // Version auf 3 aktualisieren
@TypeConverters(Converters::class)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null

        // Migration von Version 1 und 2 auf Version 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Füge die neuen Spalten zur Tabelle "quotes" hinzu
                database.execSQL("ALTER TABLE quotes ADD COLUMN html TEXT")
                database.execSQL("ALTER TABLE quotes ADD COLUMN isQuoteOfTheDay INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE quotes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quote_database"
                )
                    .addMigrations(MIGRATION_2_3) // Migration hinzufügen
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}