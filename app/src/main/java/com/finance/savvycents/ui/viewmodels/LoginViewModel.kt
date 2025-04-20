package com.finance.savvycents.ui.viewmodels

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
import com.google.firebase.firestore.DocumentSnapshot
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

    private val _emailSignInMethods = MutableLiveData<List<String>>()
    val emailSignInMethods: LiveData<List<String>> = _emailSignInMethods

    private val _loginFlow = MutableStateFlow<Resource<User>?>(null)
    val loginFlow: StateFlow<Resource<User>?> = _loginFlow

    val currentUser: FirebaseUser?
        get() = repository.currentUser

    fun checkEmail(email: String) {
        repository.checkIfEmailExists(email) { exists ->
            if (exists) {
                repository.getSignInMethodsForEmail(email) { methods ->
                    _emailSignInMethods.value = methods
                }
            } else {
                _emailSignInMethods.value = emptyList()
            }
        }
    }

    fun loginUser(email: String, password: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading()
        val result = repository.login(email, password)
        _loginFlow.value = result
    }

    fun loginWithGoogle(idToken: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading()
        val result = repository.signInWithGoogle(idToken)
        _loginFlow.value = result
    }

    fun loginWithPhone(verificationId: String, otp: String) = viewModelScope.launch {
        _loginFlow.value = Resource.Loading()
        val result = repository.verifyOtp(verificationId, otp)
        _loginFlow.value = result
    }

    fun checkUserExists(userId: String): LiveData<Boolean> {
        val exists = MutableLiveData<Boolean>()
        viewModelScope.launch {
            try {
                val userDoc = repository.getUserDocument(userId)
                exists.value = userDoc.exists()
            } catch (e: Exception) {
                exists.value = false
            }
        }
        return exists
    }

    fun validateEmail(email: String): Validator {
        return if (email.isBlank()) {
            Validator.Error("Email cannot be empty")
        } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            Validator.Error("Invalid email format")
        } else {
            Validator.Success()
        }
    }

    fun validatePassword(password: String): Validator {
        return if (password.isBlank()) {
            Validator.Error("Password cannot be empty")
        } else if (password.length < 6) {
            Validator.Error("Password must be at least 6 characters")
        } else {
            Validator.Success()
        }
    }
}