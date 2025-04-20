package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.models.User
import com.finance.savvycents.repository.AuthRepository
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.ValidationUtils
import com.finance.savvycents.utilities.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.app.Activity
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerFlow = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val registerFlow: StateFlow<Resource<Unit>> = _registerFlow

    fun register(email: String, password: String, name: String, phone: String) {
        val nameValidation = ValidationUtils.validateName(name)
        if (nameValidation is Validator.Error) {
            _registerFlow.value = Resource.Error(nameValidation.errorMsg ?: "Invalid name")
            return
        }

        val emailValidation = ValidationUtils.validateEmail(email)
        if (emailValidation is Validator.Error) {
            _registerFlow.value = Resource.Error(emailValidation.errorMsg ?: "Invalid email")
            return
        }

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (passwordValidation is Validator.Error) {
            _registerFlow.value = Resource.Error(passwordValidation.errorMsg ?: "Invalid password")
            return
        }

        val confirmPasswordValidation = ValidationUtils.validateConfirmPassword(password, password)
        if (confirmPasswordValidation is Validator.Error) {
            _registerFlow.value = Resource.Error(confirmPasswordValidation.errorMsg ?: "Passwords do not match")
            return
        }

        val phoneValidation = ValidationUtils.validatePhone(phone)
        if (phoneValidation is Validator.Error) {
            _registerFlow.value = Resource.Error(phoneValidation.errorMsg ?: "Invalid phone")
            return
        }

        viewModelScope.launch {
            _registerFlow.value = Resource.Loading()
            val result = repository.registerWithEmail(email, password, name, phone)
            if (result is Resource.Success) {
                // Send verification email
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                user?.sendEmailVerification()
            }
            _registerFlow.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Registration failed")
                else -> Resource.Error("Unexpected state")
            }
        }
    }

    fun validateName(name: String) = ValidationUtils.validateName(name)
    fun validateEmail(email: String) = ValidationUtils.validateEmail(email)
    fun validatePassword(password: String) = ValidationUtils.validatePassword(password)
    fun validateConfirmPassword(password: String, confirmPassword: String) = 
        ValidationUtils.validateConfirmPassword(password, confirmPassword)
    fun validatePhone(phone: String) = ValidationUtils.validatePhone(phone)

    fun sendOtp(phone: String, activity: Activity) {
        val phoneValidation = ValidationUtils.validatePhone(phone)
        if (phoneValidation is Validator.Error) {
            _registerFlow.value = Resource.Error(phoneValidation.errorMsg ?: "Invalid phone")
            return
        }
        viewModelScope.launch {
            _registerFlow.value = Resource.Loading()
            val result = repository.sendOtp(phone, activity)
            _registerFlow.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Failed to send OTP")
                else -> Resource.Error("Unexpected state")
            }
        }
    }
}
