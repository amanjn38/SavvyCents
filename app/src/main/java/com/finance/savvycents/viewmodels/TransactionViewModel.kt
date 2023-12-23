package com.finance.savvycents.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.models.Transaction
import com.finance.savvycents.repository.TransactionRepository
import com.finance.savvycents.utilities.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _addTransactionStatus = MutableLiveData<Resource<Unit>>()
    val addTransactionStatus: LiveData<Resource<Unit>> get() = _addTransactionStatus

    private val _transactions = MutableLiveData<Resource<List<Transaction>>>()
    val transactions: LiveData<Resource<List<Transaction>>> get() = _transactions

    fun addTransaction(userId: String, transaction: Transaction) = viewModelScope.launch {
        _addTransactionStatus.value = Resource.Loading()
        val result = repository.addTransaction(userId, transaction)
        _addTransactionStatus.value = result
    }

    fun getTransactions(userId: String) = viewModelScope.launch {
        _transactions.value = Resource.Loading()
        repository.getTransactions(userId)
            .collect { result ->
                _transactions.value = result
            }
    }
}

