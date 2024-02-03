package com.finance.savvycents.contact

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.finance.savvycents.models.ContactEntity
import com.finance.savvycents.repository.ContactRepository
import com.finance.savvycents.utilities.Resource
import javax.inject.Inject

class RemoteContactDataSource @Inject constructor(private val context: Context) : ContactRepository {

    override suspend fun getContacts(): Resource<List<ContactEntity>> {
        return try {
            val contacts = fetchContactsFromDevice()
            Resource.Success(contacts)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage)
        }
    }

    override suspend fun saveContacts(contacts: List<ContactEntity>) {
        // Saving to remote data source is not applicable in this context.
        // You can leave it as a no-op or throw an exception.
        // Example: throw UnsupportedOperationException("Remote data source does not support saving contacts.")
    }
    private suspend fun fetchContactsFromDevice(): List<ContactEntity> {
        val contacts = mutableListOf<ContactEntity>()

        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idColumnIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val contactId = if (idColumnIndex >= 0) it.getString(idColumnIndex) else ""

                val nameColumnIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val contactName = if (nameColumnIndex >= 0) it.getString(nameColumnIndex) else ""

                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )

                phoneCursor?.use { phoneDataCursor ->
                    while (phoneDataCursor.moveToNext()) {
                        val phoneNumberColumnIndex = phoneDataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneNumber = if (phoneNumberColumnIndex >= 0) phoneDataCursor.getString(phoneNumberColumnIndex) else ""

                        val contactEntity = ContactEntity(contactId.toInt(), contactName, phoneNumber)
                        contacts.add(contactEntity)
                    }
                }
                phoneCursor?.close()
            }
        }

        cursor?.close()
        return contacts
    }
}
