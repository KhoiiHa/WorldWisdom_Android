package com.example.projektworldwisdom.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.projektworldwisdom.converters.Converters
import com.example.projektworldwisdom.model.Author
import com.example.projektworldwisdom.model.Quote

@Database(entities = [Author::class, Quote::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Konverter hier registrieren
abstract class AppDatabase : RoomDatabase() {

    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}