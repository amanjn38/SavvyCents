package com.finance.savvycents.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finance.savvycents.models.ContactEntity

@Dao
interface ContactDao {
    @Query("SELECT * FROM contactentity")
    suspend fun getAllContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)
}