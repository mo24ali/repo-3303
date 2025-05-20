package com.example.mycolloc.data.local

import androidx.room.*
import com.example.mycolloc.model.Offer

@Dao
interface OfferDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer)

    @Query("SELECT * FROM Offer")
    suspend fun getAllOffers(): List<Offer>

    @Query("SELECT * FROM Offer WHERE id = :id")
    suspend fun getOfferById(id: String): Offer?
}
