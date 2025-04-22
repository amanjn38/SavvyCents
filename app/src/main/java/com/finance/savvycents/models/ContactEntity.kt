package com.finance.savvycents.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "contactentity")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val isRegisteredUser: Boolean = false
) : Parcelable