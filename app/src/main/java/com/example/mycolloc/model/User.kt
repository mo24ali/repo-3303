package com.example.mycolloc.model

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    USER,
    ADMIN
} 