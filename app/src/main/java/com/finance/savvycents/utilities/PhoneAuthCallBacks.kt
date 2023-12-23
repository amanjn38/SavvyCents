package com.finance.savvycents.utilities

import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class PhoneAuthCallbacks(private val onVerificationStateChanged: (PhoneAuthState) -> Unit) :
    PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        onVerificationStateChanged(PhoneAuthState.VerificationCompleted(credential))
    }

    override fun onVerificationFailed(e: FirebaseException) {
        onVerificationStateChanged(PhoneAuthState.VerificationFailed(e))
    }

    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        onVerificationStateChanged(PhoneAuthState.CodeSent(verificationId, token))
    }
}

sealed class PhoneAuthState {
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneAuthState()
    data class VerificationFailed(val exception: FirebaseException) : PhoneAuthState()
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : PhoneAuthState()
}