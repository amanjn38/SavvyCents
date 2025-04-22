package com.finance.savvycents

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finance.savvycents.dao.CategoryDao
import com.finance.savvycents.dao.ContactDao
import com.finance.savvycents.dao.FriendDao
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.models.category.CategoryEntity
import com.finance.savvycents.models.category.SubCategoryEntity
import com.finance.savvycents.models.FriendEntity

@Database(entities = [ContactEntity::class, CategoryEntity::class, SubCategoryEntity::class, FriendEntity::class], version = 2, exportSchema = false)
abstract class SavvyCentsDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun categoryDao(): CategoryDao
    abstract fun friendDao(): FriendDao
}