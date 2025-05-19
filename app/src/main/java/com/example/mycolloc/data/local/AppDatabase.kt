package com.example.mycolloc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mycolloc.data.local.entity.Offer

@Database(entities = [Offer::class], version = 1)
@TypeConverters(Converters::class) // ✅ Ajout ici
abstract class AppDatabase : RoomDatabase() {
    abstract fun offerDao(): OfferDao
}

