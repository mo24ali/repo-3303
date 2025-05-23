package com.example.mycolloc.util

import com.example.mycolloc.model.Offer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FavoritesManager {
    private val favoritesList = mutableListOf<Offer>()
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    fun add(offer: Offer) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (!favoritesList.any { it.id == offer.id }) {
            favoritesList.add(offer)
            dbRef.child(uid).child("favorites").child(offer.id).setValue(offer)
        }
    }

    fun remove(offer: Offer) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        favoritesList.removeAll { it.id == offer.id }
        dbRef.child(uid).child("favorites").child(offer.id).removeValue()
    }

    fun isFavorite(offer: Offer): Boolean {
        return favoritesList.any { it.id == offer.id }
    }

    fun getAll(): List<Offer> = favoritesList

    fun loadFromFirebase(onComplete: () -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        dbRef.child(uid).child("favorites").get().addOnSuccessListener { snapshot ->
            favoritesList.clear()
            for (child in snapshot.children) {
                child.getValue(Offer::class.java)?.let {
                    favoritesList.add(it)
                }
            }
            onComplete()
        }
    }
}

