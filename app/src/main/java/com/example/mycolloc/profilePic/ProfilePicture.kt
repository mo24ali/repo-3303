package com.example.mycolloc.profilePic

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_pictures")
data class ProfilePicture(
    @PrimaryKey val userId: String, // same as Firebase UID
    val localPath: String           // URI or file path of the profile picture
)