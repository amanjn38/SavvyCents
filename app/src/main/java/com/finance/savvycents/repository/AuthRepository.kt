package com.finance.savvycents.repository

import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.app.Activity
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    // Current Firebase user
    val currentUser: FirebaseUser?

    // Google
    suspend fun signInWithGoogle(idToken: String): Resource<User>
    suspend fun isUserInFirestoreByEmail(email: String): Boolean

    // Email/Password
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun signup(name: String, email: String, password: String, phone: String, user: FirebaseUser): Resource<FirebaseUser>
    suspend fun registerWithEmail(email: String, password: String, name: String, phone: String): Resource<User>
    suspend fun isUserInFirestoreByPhone(phone: String): Boolean

    // Phone/OTP
    suspend fun sendOtp(phone: String, activity: Activity): Resource<Unit>
    suspend fun verifyOtp(verificationId: String, otp: String): Resource<User>
    suspend fun linkPhoneToCurrentUser(phone: String, verificationId: String, otp: String): Resource<Unit>

    // Common
    suspend fun saveUserToFirestore(user: User): Resource<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun getUserDocument(userId: String): DocumentSnapshot {
        return FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .await()
    }
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    fun logout()
    fun checkIfEmailExists(email: String, callback: (Boolean) -> Unit)
    fun getSignInMethodsForEmail(email: String, callback: (List<String>) -> Unit)
    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit)
}