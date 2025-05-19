package com.example.mycolloc.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.example.mycolloc.model.Offer
import kotlinx.coroutines.tasks.await

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val offersRef = database.getReference("offers")

    // Authentication
    suspend fun signIn(email: String, password: String): Result<String> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: throw IllegalStateException("User ID is null after sign in")
        Result.Success(userId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun register(email: String, password: String): Result<String> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid ?: throw IllegalStateException("User ID is null after registration")
        Result.Success(userId)
    } catch (e: Exception) {
        Result.Error(e)
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

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
} 