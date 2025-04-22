package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.contact.ContactUseCase
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.utilities.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(private val contactUseCase: ContactUseCase) : ViewModel() {
    private val _contacts = MutableLiveData<Resource<List<ContactEntity>>>()
    val contacts: LiveData<Resource<List<ContactEntity>>> get() = _contacts

    fun loadContacts() {
        viewModelScope.launch {
            _contacts.value = Resource.Loading()
            System.out.println("testingLoad1")
            _contacts.value = contactUseCase.getContacts()
        }
    }

    fun checkAndAddFriend(contact: ContactEntity) {
        viewModelScope.launch {
            val isRegistered = contactUseCase.isUserRegistered(contact.email, contact.phoneNumber)
            if (isRegistered) {
                // Send friend request: add to Firestore under friend_requests
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val request = hashMapOf(
                    "from" to currentUserId,
                    "toEmail" to contact.email,
                    "toPhone" to contact.phoneNumber,
                    "status" to "pending",
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                db.collection("friend_requests").add(request)
            } else {
                // Add as local friend and mark as inviteable
                // Here you would insert into Room or local list
                // For demonstration, show a toast or log
                // TODO: Implement local friend persistence
            }
        }
    }
}
