package com.finance.savvycents.repository

import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.utilities.Resource

interface ContactRepository {
    suspend fun getContacts(): Resource<List<ContactEntity>>
    suspend fun saveContacts(contacts: List<ContactEntity>)
}
