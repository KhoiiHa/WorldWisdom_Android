package com.example.projektworldwisdom.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Keyword
import com.example.projektworldwisdom.model.Note
import com.example.projektworldwisdom.model.Quote


@Database(entities = [Quote::class, Note::class, Author::class, Keyword::class], version = 6)
@TypeConverters(Converters::class)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: QuoteDatabase? = null

        // Migration von Version 1 zu 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE quotes RENAME COLUMN _id TO id")
                database.execSQL("ALTER TABLE quotes ADD COLUMN characterCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE quotes ADD COLUMN authorImageUrl TEXT")
                database.execSQL("ALTER TABLE quotes ADD COLUMN authorId INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration von Version 2 zu 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE quotes ADD COLUMN html TEXT")
                database.execSQL("ALTER TABLE quotes ADD COLUMN isQuoteOfTheDay INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE quotes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration von Version 4 zu 5
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Neue Tabelle für Keywords erstellen
                database.execSQL("CREATE TABLE keywords_table (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, keyword TEXT NOT NULL)")

                // Temporäre Tabelle für Quotes erstellen
                database.execSQL("CREATE TABLE quotes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, content TEXT NOT NULL, authorName TEXT NOT NULL, characterCount INTEGER NOT NULL DEFAULT 0, authorImageUrl TEXT, html TEXT, isQuoteOfTheDay INTEGER NOT NULL DEFAULT 0, isFavorite INTEGER NOT NULL DEFAULT 0)")

                // Daten aus der alten Tabelle in die neue Tabelle kopieren
                database.execSQL(
                    """INSERT INTO quotes_new (id, content, authorName, characterCount, authorImageUrl, html, isQuoteOfTheDay, isFavorite) 
                    SELECT 
                        quotes.id, 
                        quotes.content, 
                        (SELECT name FROM authors_table WHERE id = quotes.authorId), 
                        characterCount, 
                        authorImageUrl, 
                        html, 
                        isQuoteOfTheDay, 
                        isFavorite 
                    FROM quotes
                """
                )

                // Alte Tabelle löschen und neue Tabelle umbenennen
                database.execSQL("DROP TABLE quotes")
                database.execSQL("ALTER TABLE quotes_new RENAME TO quotes")
            }
        }

        // Migration von Version 5 zu 6 (authorId entfernt)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Alte Tabelle für Quotes entfernen, da sie bereits in der neuen Struktur vorliegt
                database.execSQL("DROP TABLE IF EXISTS quotes")
            }
        }

        fun getDatabase(context: Context): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quote_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}