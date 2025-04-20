package com.finance.savvycents.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.SavvyCentsDatabase
import com.finance.savvycents.dao.FriendDao
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.models.FriendEntity
import com.finance.savvycents.repository.FriendsRepository
import com.finance.savvycents.repository.FriendsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(private val friendDao: FriendDao) : ViewModel() {
    private val repository: FriendsRepositoryImpl = FriendsRepositoryImpl(friendDao)

    // Function to check if a contact is already a friend
    fun checkContactStatus(contact: ContactEntity) {
        viewModelScope.launch {
            repository.checkContactStatus(contact)
        }
    }

    // Function to add a friend
    fun addFriend(friend: FriendEntity) {
        viewModelScope.launch {
            repository.addFriend(friend)
        }
    }

    // Function to send an invite
    fun sendInvite(contact: ContactEntity) {
        // Implement invite logic here
    }

    // Function to get friends for a particular user
    fun getFriends(userId: String) {
        viewModelScope.launch {
            repository.getFriends(userId)
        }
    }
}
