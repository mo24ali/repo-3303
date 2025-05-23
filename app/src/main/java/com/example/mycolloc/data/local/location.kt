package com.example.mycolloc.data.local

import java.io.Serializable

data class Location(
    val city: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
): Serializable
