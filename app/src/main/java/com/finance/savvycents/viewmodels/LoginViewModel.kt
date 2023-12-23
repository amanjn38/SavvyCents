package com.finance.savvycents.viewmodels

import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.models.User
import com.finance.savvycents.repository.AuthRepository
import com.finance.savvycents.utilities.PhoneAuthState
import com.finance.savvycents.utilities.PreferenceHelper
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.Validator
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {
    private var currentVerificationId: String? = null
//    private val _login = MutableLiveData<Resource<FirebaseUser>?>(null)
//    val login: LiveData<Resource<FirebaseUser>?> = _login
//
//    private val _signup = MutableLiveData<Resource<FirebaseUser>?>(null)
//    val signup: LiveData<Resource<FirebaseUser>?> = _signup

    private val _sendOtpStatus = MutableLiveData<Resource<PhoneAuthState>>()
    val sendOtpStatus: LiveData<Resource<PhoneAuthState>> = _sendOtpStatus

    private val _verifyOtpStatus = MutableLiveData<Resource<FirebaseUser>>()
    val verifyOtpStatus: LiveData<Resource<FirebaseUser>> = _verifyOtpStatus

    private val _resetPasswordStatus = MutableLiveData<Resource<Unit>>()
    val resetPasswordStatus: LiveData<Resource<Unit>> = _resetPasswordStatus

    private val _signUpFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signUpFlow: StateFlow<Resource<FirebaseUser>?> = _signUpFlow

    private val _loginFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Resource<FirebaseUser>?> = _loginFlow

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    init {
        if (repository.currentUser != null) {
            _loginFlow.value = Resource.Success(repository.currentUser!!)
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading()
        val result = repository.login(email, password)
        _loginFlow.value = result
    }

    fun signupUser(name: String, email: String, password: String) = viewModelScope.launch {
        _signUpFlow.value = Resource.Loading()
        val result = repository.signup(name, email, password,"123")
        _signUpFlow.value = result
    }

    fun sendForgotPasswordEmail(email: String) {
        viewModelScope.launch {
            _resetPasswordStatus.value = Resource.Loading()
            val result = repository.sendPasswordResetEmail(email)
            _resetPasswordStatus.value = result
        }
    }

    fun logout() {
        repository.logout()
        _loginFlow.value = null
        _signUpFlow.value = null
        preferenceHelper.clearLoginSession()
    }

    fun validateEmail(email: String): Validator {
        if (email.isNullOrEmpty())
            return Validator.Error("Email cannot be empty")
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches())
            return Validator.Error("Not a valid email")

        return Validator.Success()
    }

    fun validateName(name: String): Validator {
        if (name.isNullOrEmpty())
            return Validator.Error("Name cannot be empty")
        if (name.length < 4)
            return Validator.Error("Name should be greater than 4")
        return Validator.Success()
    }

    fun validatePassword(password: String): Validator {
        if (password.isNullOrEmpty())
            return Validator.Error("Password cannot be empty")

        if (password.length < 8)
            return Validator.Error("Password should be greater than 8")

        if (password.firstOrNull { it.isDigit() } == null)
            return Validator.Error("Password should contain at-least 1 digit")
        if (password.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null)
            return Validator.Error("Password should contain at-least 1 uppercase letter")

        if (password.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null)
            return Validator.Error("Password should contain at-least 1 lowercase letter")

        if (password.firstOrNull { !it.isLetterOrDigit() } == null)
            return Validator.Error("Password should contain at-least 1 special character")

        return Validator.Success()

    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Validator {

        if (confirmPassword.isNullOrEmpty())
            return Validator.Error("Confirm password cannot be empty")

        if (password != confirmPassword)
            return Validator.Error("Passwords do not match")

        return Validator.Success()
    }

    fun validatePhone(phoneNumber: String): Validator {
        if (phoneNumber.isNullOrEmpty())
            return Validator.Error("Phone number cannot be empty")

        // Remove any non-digit characters from the phone number
        val cleanedPhoneNumber = phoneNumber.replace(Regex("[^\\d]"), "")

        if (cleanedPhoneNumber.length != 10)
            return Validator.Error("Phone number should contain exactly 10 digits")

        return Validator.Success()
    }

    fun saveLoginCredential(user: User) {
        preferenceHelper.saveLoginCredential(user)
    }

    fun sendOtp(phoneNumber: String) = viewModelScope.launch {
        _sendOtpStatus.value = Resource.Loading()
        val result = repository.sendOtp(phoneNumber)
        if (result is Resource.Success) {
            when (val state = result.data) {
                is PhoneAuthState.CodeSent -> {
                    // Handle the case where the verification code is sent successfully
                    currentVerificationId = state.verificationId
                }

                is PhoneAuthState.VerificationCompleted -> {
                    // Handle the case where verification is completed automatically
                    // You may choose to sign in the user or perform other actions
                    // For example: viewModel.signInWithPhoneAuthCredential(state.credential)
                }

                is PhoneAuthState.VerificationFailed -> {
                    // Handle the case where verification fails
                    // You can display an error message or take appropriate actions
                }

                null -> {
                    // Handle the case where the state is null (unexpected)
                    // You may choose to log an error or take appropriate actions
                }
            }
        }
        _sendOtpStatus.value = result
    }

    fun verifyOtpWithCode(verificationCode: String) = viewModelScope.launch {
        _verifyOtpStatus.value = Resource.Loading()
        if (currentVerificationId != null) {
            val result = repository.verifyOtp(currentVerificationId!!, verificationCode)
            _verifyOtpStatus.value = result
        } else {
            // Handle the case where verificationId is null
            _verifyOtpStatus.value = Resource.Error("Verification failed: VerificationId is null.")
        }
    }


    fun verifyOtp(verificationId: String, otp: String) = viewModelScope.launch {
        _verifyOtpStatus.value = Resource.Loading()
        val result = repository.verifyOtp(verificationId, otp)
        _verifyOtpStatus.value = result
    }

}