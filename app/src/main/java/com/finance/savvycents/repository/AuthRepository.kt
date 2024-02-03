package com.finance.savvycents.repository

import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun signup(name: String, email: String, password: String,   phone: String, user: FirebaseUser): Resource<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    fun logout()
}