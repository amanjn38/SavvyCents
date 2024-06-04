package com.finance.savvycents.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val friendName: String,
    val friendPhoneNumber: String,
    val friendEmail: String
)
