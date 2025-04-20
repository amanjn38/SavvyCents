package com.finance.savvycents.utilities

import android.content.Context
import android.widget.Toast
import com.finance.savvycents.models.User
import com.finance.savvycents.models.category.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.ValueEventListener

fun saveUserData(context: Context, name: String, email: String, phone: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val user = User(true, userId, email, name, phone)
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
        databaseReference.child(userId).setValue(user)
            .addOnSuccessListener {
//                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
            }
    } else {
        Toast.makeText(context, "No user found", Toast.LENGTH_LONG).show()
    }
}

fun isUserLoggedIn(): Boolean {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    return currentUser != null
}

object FirestoreUtil {
    suspend fun addCategory(category: Category) {
        val categoryDocument = FirebaseFirestore.getInstance().collection("categories").document(category.id)
        categoryDocument.set(category).await()

        // Add subcategories
        for (subCategory in category.subcategories) {
            categoryDocument.collection("subcategories")
                .document(subCategory.id)
                .set(subCategory)
                .await()
        }
    }
}


fun checkIfUserExists(email: String, phone: String, callback: (Boolean) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val usersRef = database.getReference("users")

    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var userExists = false // Initialize a flag to indicate user existence
            var continueLoop = true // Initialize a flag to control loop continuation
            for (userSnapshot in dataSnapshot.children) {
                if (!continueLoop) break // Exit the loop if continueLoop is false
                val userData = userSnapshot.getValue(User::class.java)
                userData?.let {
                    if (userData.email == email || userData.phone == phone) {
                        // User exists
                        userExists = true
                        continueLoop = false // Set to false to exit loop
                    }
                }
            }
            // Invoke the callback with the result
            callback(userExists)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Handle database error
            println("Database error: ${databaseError.message}")
            // Since the operation was cancelled, assume user does not exist
            callback(false)
        }
    })
}

