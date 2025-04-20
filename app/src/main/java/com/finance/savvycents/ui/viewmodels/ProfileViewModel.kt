package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.finance.savvycents.utilities.Resource
import com.finance.savvycents.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _logoutFlow = MutableStateFlow<Resource<Unit>>(Resource.Loading())
    val logoutFlow: StateFlow<Resource<Unit>> = _logoutFlow

    private val _userData = MutableLiveData<Resource<com.finance.savvycents.models.User>>()
    val userData: LiveData<Resource<com.finance.savvycents.models.User>> = _userData

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _userData.value = Resource.Loading()
            val user = repository.getCurrentUser()
            if (user != null) {
                _userData.value = Resource.Success(user)
            } else {
                _userData.value = Resource.Error("User data not found")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutFlow.value = Resource.Loading()
            repository.logout()
            _logoutFlow.value = Resource.Success(Unit)
        }
    }
}
