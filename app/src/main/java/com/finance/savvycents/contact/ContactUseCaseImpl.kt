package com.finance.savvycents.contact

import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.repository.ContactRepository
import com.finance.savvycents.utilities.Resource

class ContactUseCaseImpl(private val repository: ContactRepository) : ContactUseCase {

    override suspend fun getContacts(): Resource<List<ContactEntity>> {
        return repository.getContacts()
    }
}