package com.finance.savvycents.repository

import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.models.FriendEntity

interface FriendsRepository {
    suspend fun checkContactStatus(contact: ContactEntity)
    suspend fun addFriend(friend: FriendEntity)
    suspend fun getFriends(userId: String)
}