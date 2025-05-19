package com.example.mycolloc.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostImageDao {
    @Query("SELECT * FROM post_images WHERE offerId = :offerId")
    fun getImagesForOffer(offerId: String): Flow<List<PostImage>>

    @Insert
    suspend fun insertImage(image: PostImage)

    @Insert
    suspend fun insertImages(images: List<PostImage>)

    @Delete
    suspend fun deleteImage(image: PostImage)

    @Query("DELETE FROM post_images WHERE offerId = :offerId")
    suspend fun deleteImagesForOffer(offerId: String)
} 