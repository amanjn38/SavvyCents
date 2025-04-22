package com.finance.savvycents.repository

import androidx.lifecycle.MutableLiveData
import com.finance.savvycents.dao.FriendDao
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.models.FriendEntity
import com.finance.savvycents.utilities.checkIfUserExists
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FriendsRepositoryImpl(
    private val friendDao: FriendDao
) : FriendsRepository {

    private val isFriend = MutableLiveData<Boolean>()
    private val friends = MutableLiveData<List<FriendEntity>>()

    override suspend fun checkContactStatus(contact: ContactEntity) {
        withContext(Dispatchers.IO) {
            try {
                checkIfUserExists(contact.email, contact.phoneNumber) {
                    if (it) {
                        isFriend.postValue(true)
                    } else {
                        isFriend.postValue(false)
                    }
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                isFriend.postValue(false)
            } catch (e: Exception) {
                isFriend.postValue(false)
            }
        }
    }

    override suspend fun addFriend(friend: FriendEntity) {
        withContext(Dispatchers.IO) {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                val friendMap = mapOf(
                    "id" to friend.id,
                    "name" to friend.name,
                    "phoneNumber" to friend.phoneNumber,
                    "email" to friend.email,
                    "isRegisteredUser" to friend.isRegisteredUser,
                    "inviteSent" to friend.inviteSent,
                    "ownerUserId" to currentUser.uid
                )

                FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.uid)
                    .collection("friends")
                    .document(friend.id.toString())
                    .set(friendMap)
                    .await()
            }

            // Add friend to local Room database
            friendDao.insertFriend(
                FriendEntity(
                    id = friend.id,
                    name = friend.name,
                    phoneNumber = friend.phoneNumber,
                    email = friend.email,
                    isRegisteredUser = friend.isRegisteredUser,
                    inviteSent = friend.inviteSent
                )
            )
        }
    }

    override suspend fun getFriends(userId: String) {
        withContext(Dispatchers.IO) {
            try {
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    suspend fun getAllFriends() : List<FriendEntity> {
        return friendDao.getAllFriends()
    }
}
