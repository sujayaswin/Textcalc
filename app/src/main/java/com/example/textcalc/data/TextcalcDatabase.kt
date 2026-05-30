package com.example.textcalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Document::class], version = 1, exportSchema = false)
abstract class TextcalcDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: TextcalcDatabase? = null

        fun getDatabase(context: Context): TextcalcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TextcalcDatabase::class.java,
                    "textcalc_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
