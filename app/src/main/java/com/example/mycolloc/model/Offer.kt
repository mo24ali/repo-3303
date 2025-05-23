package com.example.mycolloc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mycolloc.data.local.Location
import java.io.Serializable
import java.util.UUID

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val images: List<String> = emptyList(),
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val location: Location = Location(),
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val surface: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
): Serializable {
    // Empty constructor for Firebase
    constructor() : this(
        id = "",
        userId = "",
        title = "",
        description = "",
        price = 0.0,
        category = "",
        latitude = 0.0,
        longitude = 0.0,
        images = emptyList(),
        imageUrl = "",
        isActive = true,
        location = Location(),
        bedrooms = 0,
        bathrooms = 0,
        surface = "",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

//data class Location(
//    val latitude: Double = 0.0,
//    val longitude: Double = 0.0,
//    val address: String = "",
//    val city: String = ""
//)

data class Negotiation(
    val id: String = "",
    val offerId: String = "",
    val userId: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val message: String = "",
    val status: NegotiationStatus = NegotiationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class OfferStatus {
    ACTIVE,
    SOLD,
    EXPIRED,
    DELETED
}

enum class NegotiationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
} 