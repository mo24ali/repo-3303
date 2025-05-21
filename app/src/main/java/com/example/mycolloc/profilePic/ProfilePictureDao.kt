package com.example.mycolloc.profilePic

import androidx.room.*

@Dao
interface ProfilePictureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profilePicture: ProfilePicture)

    @Query("SELECT * FROM profile_pictures WHERE userId = :userId LIMIT 1")
    suspend fun getProfilePicture(userId: String): ProfilePicture?

    @Delete
    suspend fun delete(profilePicture: ProfilePicture)
}