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


@Database(entities = [Quote::class, Note::class, Author::class], version = 4)
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
                // Spalte umbenennen
                database.execSQL("ALTER TABLE quotes RENAME COLUMN _id TO id")

                // Temporäre Tabelle erstellen, um Spalten zu entfernen und neue hinzuzufügen
                database.execSQL("CREATE TABLE quotes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, content TEXT NOT NULL, author TEXT NOT NULL, authorId INTEGER NOT NULL, characterCount INTEGER NOT NULL DEFAULT 0, authorImageUrl TEXT)")

                // Daten aus der alten Tabelle in die neue Tabelle kopieren (mit 'authorId')
                // Hier verwenden wir eine Subquery, um die authorId basierend auf dem Autorennamen zu finden
                database.execSQL(
                    """INSERT INTO quotes_new (id, content, author, authorId, characterCount, authorImageUrl) 
                SELECT 
                    quotes.id, 
                    quotes.content, 
                    quotes.author, 
                    (SELECT id FROM authors_table WHERE name = quotes.author), 
                    length(content), 
                    quotes.authorImageUrl  // Oder einen Standardwert, falls die Spalte nicht existierte
                FROM quotes
            """
                )

                // Alte Tabelle löschen und neue Tabelle umbenennen
                database.execSQL("DROP TABLE quotes")
                database.execSQL("ALTER TABLE quotes_new RENAME TO quotes")

                // Fremdschlüssel hinzufügen (nicht mehr nötig, da bereits in der neuen Tabelle definiert)
                // database.execSQL("ALTER TABLE quotes ADD FOREIGN KEY(authorId) REFERENCES authors_table(id) ON DELETE CASCADE")
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

        fun getDatabase(context: Context): QuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuoteDatabase::class.java,
                    "quote_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Beide Migrationen hinzufügen
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}