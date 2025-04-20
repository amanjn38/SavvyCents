package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.repository.AuthRepository
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.utilities.Validator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _resetPasswordStatus = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val resetPasswordStatus: StateFlow<Resource<Unit>> = _resetPasswordStatus

    fun sendForgotPasswordEmail(email: String) {
        if (validateEmail(email) is Validator.Error) {
            _resetPasswordStatus.value = Resource.Error("Invalid email format")
            return
        }

        viewModelScope.launch {
            _resetPasswordStatus.value = Resource.Loading()
            val result = repository.sendPasswordResetEmail(email)
            _resetPasswordStatus.value = when (result) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message ?: "Unknown error")
                else -> Resource.Loading()
            }
        }
    }

    private fun validateEmail(email: String): Validator {
        if (email.isEmpty()) {
            return Validator.Error("Email cannot be empty")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Validator.Error("Invalid email format")
        }
        return Validator.Success()
    }
}
