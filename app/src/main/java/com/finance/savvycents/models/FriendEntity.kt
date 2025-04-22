package com.finance.savvycents.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val isRegisteredUser: Boolean = false,
    val inviteSent: Boolean = false
)
