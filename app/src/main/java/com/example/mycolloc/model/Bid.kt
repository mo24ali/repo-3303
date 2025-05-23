package com.example.mycolloc.model

data class Bid(
    val bidId: String = "",
    val userId: String = "",
    val offerId: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0,
    val status: String? = null // ex : Accepted, Pending, Rejected
)
