package com.finance.savvycents.repository

import android.app.Activity
import android.content.Context
import com.finance.savvycents.SavvyCentsApp
import com.finance.savvycents.utilities.PhoneAuthCallbacks
import com.finance.savvycents.utilities.PhoneAuthState
import com.finance.savvycents.utilities.Resource
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val context : Context
) : AuthRepository {
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message)
        }
    }

    override suspend fun signup(
        name: String,
        email: String,
        password: String,
        phone: String
    ): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result?.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(name).build()
            )?.await()
            sendOtp(phone)
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message)
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

    override suspend fun sendOtp(phoneNumber: String): Resource<PhoneAuthState> {
        return try {
            lateinit var verificationId: String
            lateinit var token: PhoneAuthProvider.ForceResendingToken
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(context as Activity)
                .setCallbacks(
                    PhoneAuthCallbacks { state ->
                        when (state) {
                            is PhoneAuthState.CodeSent -> {
                                verificationId = state.verificationId
                                token = state.token
                            }
                            // Handle other states if needed
                            is PhoneAuthState.VerificationCompleted -> TODO()
                            is PhoneAuthState.VerificationFailed -> TODO()
                        }
                    }
                )
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
            Resource.Success(PhoneAuthState.CodeSent(verificationId, token))
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): Resource<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            firebaseAuth.signInWithCredential(credential).await()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                Resource.Success(currentUser)
            } else {
                Resource.Error("Verification failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message)
        }
    }

}