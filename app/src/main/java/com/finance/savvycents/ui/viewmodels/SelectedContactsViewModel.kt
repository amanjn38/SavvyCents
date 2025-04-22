package com.finance.savvycents.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.finance.savvycents.models.ContactEntity

class SelectedContactsViewModel : ViewModel() {
    private val _selectedContacts = MutableLiveData<List<ContactEntity>>(emptyList())
    val selectedContacts: LiveData<List<ContactEntity>> get() = _selectedContacts

    fun setSelectedContacts(contacts: List<ContactEntity>) {
        _selectedContacts.value = contacts
    }
}
