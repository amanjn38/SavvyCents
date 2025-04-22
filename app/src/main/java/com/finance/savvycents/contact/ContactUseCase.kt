package com.finance.savvycents.contact

import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.utilities.Resource


interface ContactUseCase {
    suspend fun getContacts(): Resource<List<ContactEntity>>
    suspend fun isUserRegistered(email: String, phone: String): Boolean
}
