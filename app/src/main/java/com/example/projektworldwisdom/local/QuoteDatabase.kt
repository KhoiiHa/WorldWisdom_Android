package com.example.projektworldwisdom.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Note
import com.example.projektworldwisdom.model.Quote



@Database(entities = [Quote::class, Note::class, Author::class], version = 3) // Author-Entität hinzugefügt
@TypeConverters(Converters::class)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
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
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}