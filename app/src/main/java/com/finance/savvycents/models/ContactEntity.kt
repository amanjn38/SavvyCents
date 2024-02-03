package com.finance.savvycents.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactentity")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val phoneNumber: String
)