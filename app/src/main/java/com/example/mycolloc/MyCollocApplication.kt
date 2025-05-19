package com.example.mycolloc

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyCollocApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase Database persistence
        Firebase.database.setPersistenceEnabled(true)
    }
} 