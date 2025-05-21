package com.example.mycolloc.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.example.mycolloc.model.Offer
import com.example.mycolloc.model.User
import com.example.mycolloc.model.UserRole
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data class RecaptchaRequired(val activity: Activity) : Result<Nothing>()
}

class FirebaseRepository {
    private val auth = Firebase.auth
    private val database = Firebase.database
    private val offersRef = database.getReference("offers")
    private val usersRef = database.getReference("users")

    init {
        // Keep data synced
        database.reference.keepSynced(true)
    }

    // Authentication
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        activity: Activity
    ): Result<String> = try {
        try {
            android.util.Log.d("FirebaseRepository", "Starting registration for email: $email")
            
            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw IllegalStateException("User ID is null after registration")
            android.util.Log.d("FirebaseRepository", "User created in Auth with ID: $userId")

            // Create user profile in Realtime Database
            val user = User(
                id = userId,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                role = UserRole.USER,
                createdAt = System.currentTimeMillis()
            )

            // Store user data in Realtime Database
            android.util.Log.d("FirebaseRepository", "Storing user data in Database for ID: $userId")
            usersRef.child(userId).setValue(user).await()
            android.util.Log.d("FirebaseRepository", "User data stored successfully")

            Result.Success(userId)
        } catch (e: FirebaseAuthException) {
            android.util.Log.e("FirebaseRepository", "Firebase Auth Exception during registration", e)
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> {
                    Result.RecaptchaRequired(activity)
                }
                else -> Result.Error(e)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("FirebaseRepository", "Exception during registration", e)
        Result.Error(e)
    }

    suspend fun signIn(email: String, password: String, activity: Activity): Result<User> = try {
        try {
            android.util.Log.d("FirebaseRepository", "Starting sign in for email: $email")
            
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw IllegalStateException("User ID is null after sign in")
            android.util.Log.d("FirebaseRepository", "User signed in with ID: $userId")
            
            // Get user data from Realtime Database using KTX API
            android.util.Log.d("FirebaseRepository", "Fetching user data from Database")
            val userSnapshot = usersRef.child(userId).get().await()
            val user = userSnapshot.getValue<User>()
            
            if (user == null) {
                android.util.Log.e("FirebaseRepository", "User data not found in Database for ID: $userId")
                throw IllegalStateException("User data not found")
            }
            
            android.util.Log.d("FirebaseRepository", "User data retrieved successfully")
            Result.Success(user)
        } catch (e: FirebaseAuthException) {
            android.util.Log.e("FirebaseRepository", "Firebase Auth Exception during sign in", e)
            when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> {
                    Result.RecaptchaRequired(activity)
                }
                else -> Result.Error(e)
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("FirebaseRepository", "Exception during sign in", e)
        Result.Error(e)
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUser(): Result<User> = try {
        val userId = getCurrentUserId() ?: throw IllegalStateException("No user logged in")
        val userSnapshot = usersRef.child(userId).get().await()
        val user = userSnapshot.getValue<User>() ?: throw IllegalStateException("User data not found")
        Result.Success(user)
        } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null
    ): Result<User> = try {
        val userId = getCurrentUserId() ?: throw IllegalStateException("No user logged in")
        val updates = mutableMapOf<String, Any>()
        
        firstName?.let { updates["firstName"] = it }
        lastName?.let { updates["lastName"] = it }
        phoneNumber?.let { updates["phoneNumber"] = it }
        
        if (updates.isNotEmpty()) {
            usersRef.child(userId).updateChildren(updates).await()
        }
        
        // Get updated user data
        getCurrentUser()
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun signOut() {
        auth.signOut()
    }

    // Offers
    suspend fun createOffer(offer: Offer): Result<String> = try {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User must be logged in to create an offer")
        val offerWithId = offer.copy(
            id = offersRef.push().key ?: throw IllegalStateException("Failed to generate offer ID"),
            userId = userId
        )
        offersRef.child(offerWithId.id).setValue(offerWithId).await()
        Result.Success(offerWithId.id)
        } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOffers(): Result<List<Offer>> = try {
        val snapshot = offersRef.get().await()
        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }
        Result.Success(offers)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOffer(id: String): Result<Offer> = try {
        val snapshot = offersRef.child(id).get().await()
        val offer = snapshot.getValue<Offer>() ?: throw IllegalStateException("Offer not found")
        Result.Success(offer)
        } catch (e: Exception) {
        Result.Error(e)
    }

//    fun getNearbyOffers(latitude: Double, longitude: Double, radiusInKm: Double): Flow<List<Offer>> = flow {
//        try {
//            // Convert radius from km to degrees (approximate)
//            val radiusInDegrees = radiusInKm / 111.0 // 1 degree ≈ 111 km at the equator
//
//            // Calculate bounding box
//            val minLat = latitude - radiusInDegrees
//            val maxLat = latitude + radiusInDegrees
//            val minLng = longitude - radiusInDegrees
//            val maxLng = longitude + radiusInDegrees
//
//            // Query offers within the bounding box
//            val offersQuery = offersRef
//                .orderByChild("location/latitude")
//                .startAt(minLat)
//                .endAt(maxLat)
//                .orderByChild("location/longitude")
//                .startAt(minLng)
//                .endAt(maxLng)
//                .get()
//
//            val offers = offersQuery.await().children.mapNotNull { it.getValue<Offer>() }
//
//            // Filter offers by actual distance (since bounding box is approximate)
//            val nearbyOffers = offers.filter { offer ->
//                val offerLat = offer.latitude
//                val offerLng = offer.longitude
//                if (offerLat != null && offerLng != null) {
//                    val distance = calculateDistance(
//                        latitude, longitude,
//                        offerLat, offerLng
//                    )
//                    distance <= radiusInKm
//                } else {
//                    false
//                }
//            }
//
//            emit(nearbyOffers)
//        } catch (e: Exception) {
//            throw e
//        }
//    }
fun getNearbyOffers(latitude: Double, longitude: Double, radiusInKm: Double): Flow<List<Offer>> = flow {
    try {
        val radiusInDegrees = radiusInKm / 111.0

        val minLat = latitude - radiusInDegrees
        val maxLat = latitude + radiusInDegrees


        val snapshot = offersRef
            .orderByChild("latitude")
            .startAt(minLat)
            .endAt(maxLat)
            .get()
            .await()

        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }

        val nearbyOffers = offers.filter { offer ->
            val offerLat = offer.latitude
            val offerLng = offer.longitude
            val distance = calculateDistance(latitude, longitude, offerLat, offerLng)
            distance <= radiusInKm
        }

        emit(nearbyOffers)
    } catch (e: Exception) {
        throw e
    }
}


    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return r * c
    }

    suspend fun deleteOffer(offerId: String): Result<Unit> = try {
        // First verify that the current user is the owner of the offer
        val currentUserId = getCurrentUserId() ?: throw IllegalStateException("User must be logged in to delete an offer")
        
        // Get the offer to verify ownership
        val offerSnapshot = offersRef.child(offerId).get().await()
        val offer = offerSnapshot.getValue<Offer>() ?: throw IllegalStateException("Offer not found")
        
        // Check if the current user is the owner of the offer
        if (offer.userId != currentUserId) {
            throw SecurityException("You don't have permission to delete this offer")
        }
        
        // Delete the offer
        offersRef.child(offerId).removeValue().await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // User operations
    suspend fun getUser(userId: String): User? {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            snapshot.getValue<User>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUser(user: User): Result<String> = try {
        usersRef.child(user.id).setValue(user).await()
        Result.Success(user.id)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Offer operations
    suspend fun getAllOffers(): Result<List<Offer>> = try {
        val snapshot = offersRef.get().await()
        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }
        Result.Success(offers)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun searchOffers(query: String): Result<List<Offer>> = try {
        val snapshot = offersRef.get().await()
        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }
            .filter { offer ->
                offer.title.contains(query, ignoreCase = true) ||
                offer.description.contains(query, ignoreCase = true) ||
                offer.category.contains(query, ignoreCase = true)
            }
        Result.Success(offers)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOffersByCategory(category: String): Result<List<Offer>> = try {
        val snapshot = offersRef
            .orderByChild("category")
            .equalTo(category)
            .get()
            .await()
        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }
            .filter { it.isActive }
        Result.Success(offers)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOffersByPriceRange(minPrice: Double, maxPrice: Double): Result<List<Offer>> = try {
        val snapshot = offersRef
            .orderByChild("price")
            .startAt(minPrice)
            .endAt(maxPrice)
            .get()
            .await()
        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }
            .filter { it.isActive }
        Result.Success(offers)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getOffersByLocation(latitude: Double, longitude: Double, radiusInKm: Double): Result<List<Offer>> = try {
        val snapshot = offersRef.get().await()
        val offers = snapshot.children.mapNotNull { it.getValue<Offer>() }
            .filter { offer ->
                offer.latitude != null && offer.longitude != null &&
                calculateDistance(latitude, longitude, offer.latitude!!, offer.longitude!!) <= radiusInKm
            }
        Result.Success(offers)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun updateOfferStatus(offerId: String, isActive: Boolean): Result<Offer> = try {
        val updates = mapOf("isActive" to isActive, "updatedAt" to System.currentTimeMillis())
        offersRef.child(offerId).updateChildren(updates).await()
        val snapshot = offersRef.child(offerId).get().await()
        val offer = snapshot.getValue<Offer>() ?: throw IllegalStateException("Offer not found")
        Result.Success(offer)
    } catch (e: Exception) {
        Result.Error(e)
    }
} 