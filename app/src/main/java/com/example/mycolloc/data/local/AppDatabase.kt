package com.example.mycolloc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PostImage::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postImageDao(): PostImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mycolloc_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
