package com.example.mycolloc

import android.app.Application
import com.example.mycolloc.data.local.AppDatabase
import com.google.firebase.database.FirebaseDatabase

class MyCollocApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
} 