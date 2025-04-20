package com.finance.savvycents.repository

import com.finance.savvycents.models.Transaction
import com.finance.savvycents.utilities.Resource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(private val firestore: FirebaseFirestore) : TransactionRepository {

    private val transactionsCollection: CollectionReference = firestore.collection("transactions")

    override suspend fun addTransaction(userId: String, transaction: Transaction): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                System.out.println("testing1" + userId)

                transactionsCollection.document(userId)
                    .collection("user_transactions")
                    .add(transaction)
                    .await()
                Resource.Success(Unit)
            } catch (e: Exception) {
                System.out.println("testing11" + userId)

                Resource.Error("Failed to add transaction: ${e.message}")
            }
        }
    }

    override fun getTransactions(userId: String): Flow<Resource<List<Transaction>>> = callbackFlow {
        val listenerRegistration = transactionsCollection.document(userId)
            .collection("user_transactions")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Resource.Error("Failed to get transactions: ${error.message}"))
                    return@addSnapshotListener
                }

                val transactions = value?.documents?.mapNotNull { document ->
                    document.toObject(Transaction::class.java)
                } ?: emptyList()

                trySend(Resource.Success(transactions))
            }

        awaitClose {
            // Remove the listener when the coroutine is cancelled
            listenerRegistration.remove()
        }
    }
}
