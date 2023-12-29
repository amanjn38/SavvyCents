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
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    private val _loginFlow = MutableStateFlow<Resource<User>?>(null)
    val loginFlow: StateFlow<Resource<User>?> = _loginFlow

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    init {
        viewModelScope.launch {
            fetchUserDetails()
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading()
        val result = repository.login(email, password)
        _loginFlow.value = result
    }

    fun signupUser(
        name: String,
        email: String,
        password: String,
        phoneNumber: String,
        user: FirebaseUser
    ) = viewModelScope.launch {
        _signUpFlow.value = Resource.Loading()
        val result = repository.signup(name, email, password, phoneNumber, user)
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

    private suspend fun fetchUserDetails() {
        if (repository.currentUser != null) {
            val uid = repository.currentUser?.uid

            // Check if uid is not null
            if (uid != null) {
                try {
                    // Fetch user details from Firebase Database
                    val userSnapshot =
                        FirebaseDatabase.getInstance().reference.child("users").child(uid).get().await()

                    // Check if user data exists in the database
                    if (userSnapshot.exists()) {
                        val user = userSnapshot.getValue(User::class.java)
                        _loginFlow.value = Resource.Success(user!!)
                    } else {
                        _loginFlow.value =
                            Resource.Error("User data not found in the database", null)
                    }
                } catch (e: Exception) {
                    _loginFlow.value = Resource.Error("Error fetching user details", null)
                }
            } else {
                _loginFlow.value = Resource.Error("User uid is null", null)
            }
        }
    }
}