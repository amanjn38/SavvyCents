package com.finance.savvycents

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finance.savvycents.dao.ContactDao
import com.finance.savvycents.dao.FriendDao
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.models.FriendEntity

@Database(entities = [ContactEntity::class, FriendEntity::class], version = 1, exportSchema = false)
abstract class SavvyCentsDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun friendDao(): FriendDao
}