package com.finance.savvycents.repository

import android.app.Activity
import android.content.Context
import com.finance.savvycents.models.User
import com.finance.savvycents.utilities.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Fetch user profile from Realtime Database
                val snapshot = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(firebaseUser.uid)
                    .get()
                    .await()
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    Resource.Success(user)
                } else {
                    Resource.Error("User data not found")
                }
            } else {
                Resource.Error("Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
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
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun saveUserToFirestore(user: User): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save user data")
        }
    }

    override fun getSignInMethodsForEmail(email: String, callback: (List<String>) -> Unit) {
        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result?.signInMethods ?: emptyList())
            } else {
                callback(emptyList())
            }
        }
    }

    override fun checkIfEmailExists(email: String, callback: (Boolean) -> Unit) {
        firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result?.signInMethods?.isNotEmpty() == true)
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
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send reset email")
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun getUserDocument(userId: String): DocumentSnapshot {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        phone: String
    ): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Registration failed")
            val user = User(
                isLoggedIn = true,
                userId = firebaseUser.uid,
                email = email,
                name = name,
                phone = phone
            )
            // Save to Realtime Database
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.uid)
                .setValue(user)
                .await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun isUserInFirestoreByEmail(email: String): Boolean {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .orderByChild("email")
                .equalTo(email)
                .get()
                .await()
            snapshot.exists() && snapshot.childrenCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isUserInFirestoreByPhone(phone: String): Boolean {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .orderByChild("phone")
                .equalTo(phone)
                .get()
                .await()
            snapshot.exists() && snapshot.childrenCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Google sign-in failed")
            // Fetch user profile from Realtime Database
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.uid)
                .get()
                .await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User profile not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google sign-in failed")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun sendOtp(phone: String, activity: Activity): Resource<Unit> =
        suspendCancellableCoroutine { cont ->
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                        cont.resume(Resource.Success(Unit), null)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        cont.resume(Resource.Error(e.message ?: "OTP sending failed"), null)
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        cont.resume(Resource.Success(Unit), null)
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    override suspend fun verifyOtp(verificationId: String, otp: String): Resource<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Verification failed")
            // Fetch user profile from Realtime Database
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.uid)
                .get()
                .await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User profile not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "OTP verification failed")
        }
    }

    override suspend fun linkPhoneToCurrentUser(
        phone: String,
        verificationId: String,
        otp: String
    ): Resource<Unit> {
        val user = firebaseAuth.currentUser ?: return Resource.Error("No user logged in")
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            user.linkWithCredential(credential).await()
            // Optionally, update phone in Realtime Database
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.uid)
                .child("phone")
                .setValue(phone)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to link phone")
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseUser.uid)
                .get()
                .await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}