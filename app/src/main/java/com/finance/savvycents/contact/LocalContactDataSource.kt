package com.finance.savvycents.contact

import com.finance.savvycents.dao.ContactDao
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.repository.ContactRepository
import com.finance.savvycents.utilities.Resource
import javax.inject.Inject

class LocalContactDataSource @Inject constructor(private val contactDao: ContactDao) : ContactRepository {

    override suspend fun getContacts(): Resource<List<ContactEntity>> {
        return try {
            val localContacts = contactDao.getAllContacts()
            if (localContacts.isNotEmpty()) {
                Resource.Success(localContacts)
            } else {
                Resource.Error("Local database is empty")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun saveContacts(contacts: List<ContactEntity>) {
        contactDao.insertContacts(contacts)
    }
}
