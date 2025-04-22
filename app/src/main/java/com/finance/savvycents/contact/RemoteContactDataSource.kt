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

                val nameColumnIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val contactName = if (nameColumnIndex >= 0) it.getString(nameColumnIndex) else ""

                // Fetch phone number
                var phoneNumber = ""
                val hasPhoneNumberIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val hasPhoneNumber = if (hasPhoneNumberIndex >= 0) it.getInt(hasPhoneNumberIndex) > 0 else false
                if (hasPhoneNumber) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            val phoneNumberColumnIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            phoneNumber = if (phoneNumberColumnIndex >= 0) pc.getString(phoneNumberColumnIndex) else ""
                        }
                    }
                }

                // Fetch email address
                var email = ""
                val emailCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(contactId),
                    null
                )
                emailCursor?.use { ec ->
                    if (ec.moveToFirst()) {
                        val emailIndex = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                        email = if (emailIndex >= 0) ec.getString(emailIndex) else ""
                    }
                }

                // Only add contacts with at least one identifier
                if (contactName.isNotBlank() || phoneNumber.isNotBlank() || email.isNotBlank()) {
                    contacts.add(ContactEntity(contactId.toIntOrNull() ?: 0, contactName, phoneNumber, email))
                }
            }
        }

        cursor?.close()
        return contacts
    }
}
