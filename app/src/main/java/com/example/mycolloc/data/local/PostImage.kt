package com.example.mycolloc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_images")
data class PostImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val offerId: String,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis()
) 