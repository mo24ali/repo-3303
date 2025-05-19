package com.example.mycolloc.data.local.entity


import androidx.room.*
import java.util.UUID

@Entity
data class Offer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<String> = emptyList(), // 📸 images stockées
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@TypeConverter
fun fromListToString(value: List<String>): String = value.joinToString(",")

@TypeConverter
fun fromStringToList(value: String): List<String> = value.split(",").filter { it.isNotBlank() }
