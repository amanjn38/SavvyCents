package com.finance.savvycents.repository

import android.content.Context
import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : AuthRepository {
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid

            // Check if uid is not null
            if (uid != null) {
                // Fetch user details from Firebase Database
                val userSnapshot =
                    FirebaseDatabase.getInstance().reference.child("users").child(uid).get().await()

                // Check if user data exists in the database
                if (userSnapshot.exists()) {
                    val user = userSnapshot.getValue(User::class.java)
                    Resource.Success(user!!)
                } else {
                    Resource.Error("User data not found in the database")
                }
            } else {
                Resource.Error("User uid is null")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message)
        }
    }

    override suspend fun signup(
        name: String,
        email: String,
        password: String,
        phone: String,
        user: FirebaseUser
    ): Resource<FirebaseUser> {
        return try {
            val emailCredential = EmailAuthProvider.getCredential(email, password)
            user.linkWithCredential(emailCredential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    System.out.println("Account Created")
                } else if (task.isCanceled) {
                    System.out.println("Account creation cancelled")
                } else if (task.isComplete) {
                    System.out.println("Account creation completed")
                } else {
                    val message = task.exception
                    System.out.println("Task creation failed " + message)
                }
            }
            Resource.Success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message)
        }
    }

    override fun checkIfEmailExists(email: String, callback: (Boolean) -> Unit) {
        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result.signInMethods?.isNotEmpty() == true)
            } else {
                callback(false)
            }
        }
    }

    override fun registerUser(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    override suspend fun sendPasswordResetEmail(
        email: String
    ): Resource<Unit> {
        return try {
            val signInMethods = suspendCoroutine<SignInMethodQueryResult?> { continuation ->
                firebaseAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener { task ->
                        continuation.resume(task.result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            val isCustomAuth =
                signInMethods?.signInMethods?.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)
                    ?: false

            val errorMessage = "Cannot send reset email"
            val emailNotRegisteredMessage = "Email ID not registered"
            if (isCustomAuth) {
                firebaseAuth.sendPasswordResetEmail(email).await()
                Resource.Success(Unit)
            } else {
                if (signInMethods == null || signInMethods.signInMethods.isNullOrEmpty()) {
                    Resource.Error(emailNotRegisteredMessage)
                } else {
                    Resource.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message)
        }
    }

    override fun logout() {

        firebaseAuth.signOut()
    }
}