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
                // Add friend to Firebase Firestore
                val friendMap = mapOf(
                    "id" to friend.id,
                    "name" to friend.friendName,
                    "phoneNumber" to friend.friendPhoneNumber,
                    "email" to friend.friendEmail
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
                    friendName = friend.friendName,
                    friendPhoneNumber = friend.friendPhoneNumber,
                    friendEmail = friend.friendEmail
                )
            )
        }
    }

    override suspend fun getFriends(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Retrieve friends from local Room database
                val friendsFromDatabase = friendDao.getFriends(userId)

                // Update LiveData with the list of friends
                friends.postValue(friendsFromDatabase)
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }
}
