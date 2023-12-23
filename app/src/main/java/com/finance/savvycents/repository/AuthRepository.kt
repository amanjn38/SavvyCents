package com.finance.savvycents.repository

import com.finance.savvycents.utilities.PhoneAuthState
import com.finance.savvycents.utilities.Resource
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Resource<FirebaseUser>
    suspend fun signup(name: String, email: String, password: String, phone: String): Resource<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun sendOtp(phoneNumber: String): Resource<PhoneAuthState>
    suspend fun verifyOtp(verificationId: String, otp: String): Resource<FirebaseUser>
    fun logout()
}