package com.example.mycolloc.data.local

import androidx.room.*
import com.example.mycolloc.model.Offer
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer)

    @Query("SELECT * FROM offers")
    fun getAllOffers(): Flow<List<Offer>>

    @Query("SELECT * FROM offers WHERE id = :id")
    suspend fun getOfferById(id: String): Offer?

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun deleteOffer(id: String)

    @Query("UPDATE offers SET isActive = :isActive WHERE id = :id")
    suspend fun updateOfferStatus(id: String, isActive: Boolean)
}
