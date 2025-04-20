package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.contact.ContactUseCase
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.utilities.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(private val contactUseCase: ContactUseCase) : ViewModel() {
    private val _contacts = MutableLiveData<Resource<List<ContactEntity>>>()
    val contacts: LiveData<Resource<List<ContactEntity>>> get() = _contacts

    fun loadContacts() {
        viewModelScope.launch {
            _contacts.value = Resource.Loading()
            _contacts.value = contactUseCase.getContacts()
        }
    }
}
