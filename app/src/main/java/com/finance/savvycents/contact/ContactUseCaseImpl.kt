package com.finance.savvycents.contact

import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.repository.ContactRepository
import com.finance.savvycents.utilities.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactUseCaseImpl(private val repository: ContactRepository) : ContactUseCase {

    override suspend fun getContacts(): Resource<List<ContactEntity>> {
        return repository.getContacts()
    }

    override suspend fun isUserRegistered(email: String, phone: String): Boolean {
        val db = FirebaseFirestore.getInstance()
        // Assume users are stored in collection "users" with fields "email" and "phoneNumber"
        val emailQuery = db.collection("users").whereEqualTo("email", email).limit(1).get().await()
        if (!email.isNullOrBlank() && !emailQuery.isEmpty) return true
        if (!phone.isNullOrBlank()) {
            val phoneQuery = db.collection("users").whereEqualTo("phoneNumber", phone).limit(1).get().await()
            if (!phoneQuery.isEmpty) return true
        }
        return false
    }
}