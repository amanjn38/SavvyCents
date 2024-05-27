package com.finance.savvycents.utilities

import android.content.Context
import android.widget.Toast
import com.finance.savvycents.models.User
import com.finance.savvycents.models.category.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

