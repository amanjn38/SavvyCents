package com.finance.savvycents.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.finance.savvycents.models.FriendEntity

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("SELECT * FROM friends")
    suspend fun getAllFriends(): List<FriendEntity>
}