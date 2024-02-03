package com.finance.savvycents

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finance.savvycents.dao.ContactDao
import com.finance.savvycents.models.ContactEntity

@Database(entities = [ContactEntity::class], version = 1, exportSchema = false)
abstract class SavvyCentsDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}