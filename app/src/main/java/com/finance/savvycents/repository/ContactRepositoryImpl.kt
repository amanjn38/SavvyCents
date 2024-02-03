package com.finance.savvycents.repository

import com.finance.savvycents.contact.LocalContactDataSource
import com.finance.savvycents.contact.RemoteContactDataSource
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.utilities.Resource

class ContactRepositoryImpl(
    private val remoteDataSource: RemoteContactDataSource,
    private val localDataSource: LocalContactDataSource
) : ContactRepository {

    override suspend fun getContacts(): Resource<List<ContactEntity>> {
        val localContacts = localDataSource.getContacts()
        return if (localContacts is Resource.Success && localContacts.data!!.isNotEmpty()) {
            localContacts
        } else {
            val remoteContacts = remoteDataSource.getContacts()
            if (remoteContacts is Resource.Success) {
                remoteContacts.data?.let { localDataSource.saveContacts(it) }
            }
            remoteContacts
        }
    }

    override suspend fun saveContacts(contacts: List<ContactEntity>) {
        localDataSource.saveContacts(contacts)
    }
}
