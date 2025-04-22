package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.dao.FriendDao
import com.finance.savvycents.models.FriendEntity
import com.finance.savvycents.repository.FriendsRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(private val friendDao: FriendDao) : ViewModel() {
    private val repository: FriendsRepositoryImpl = FriendsRepositoryImpl(friendDao)

    private val _friends = MutableStateFlow<List<FriendEntity>>(emptyList())
    val friends: StateFlow<List<FriendEntity>> = _friends.asStateFlow()

    init {
        loadFriendsWithLocalFallback()
    }

    fun loadFriendsWithLocalFallback() {
        viewModelScope.launch {
            val localFriends = friendDao.getAllFriends()
            if (localFriends.isNotEmpty()) {
                _friends.value = localFriends
            } else {
                val remoteFriends = fetchFriendsFromFirestore()
                if (remoteFriends.isNotEmpty()) {
                    remoteFriends.forEach { friendDao.insertFriend(it) }
                    _friends.value = remoteFriends
                } else {
                    _friends.value = emptyList()
                }
            }
        }
    }

    private suspend fun fetchFriendsFromFirestore(): List<FriendEntity> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val uid = currentUser?.uid ?: return emptyList()
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("friends")
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FriendEntity::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addFriend(friend: FriendEntity) {
        viewModelScope.launch {
            repository.addFriend(friend)
            loadFriendsWithLocalFallback()
        }
    }
}
