package com.example.mycolloc.model

data class Offer(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val userId: String = "",
    val location: Location = Location(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = ""
)

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