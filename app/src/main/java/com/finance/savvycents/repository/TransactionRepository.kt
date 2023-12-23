package com.finance.savvycents.repository

import com.finance.savvycents.models.Transaction
import com.finance.savvycents.utilities.Resource
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun addTransaction(userId: String, transaction: Transaction): Resource<Unit>
    fun getTransactions(userId: String): Flow<Resource<List<Transaction>>>
}