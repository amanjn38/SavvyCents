package com.finance.savvycents.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.databinding.TransactionItemBinding
import com.finance.savvycents.models.Transaction
import com.finance.savvycents.utilities.DataDiffCallback
class TransactionAdapter() :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private val transactionList = mutableListOf<Transaction>()

    private var transactions = mutableListOf<Transaction>()

    inner class TransactionViewHolder(private val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.apply {
                typeTextView.text = transaction.type
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TransactionItemBinding.inflate(inflater, parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactions[position]
        holder.bind(currentTransaction)
    }

    fun submitList(newList: List<Transaction>) {
        DiffUtil.calculateDiff(DataDiffCallback(transactions, newList)).dispatchUpdatesTo(this)
        transactions.clear()
        transactions.addAll(newList)
    }

    fun clearList() {
        val itemCount = transactionList.size
        transactionList.clear()
        notifyItemRangeRemoved(0, itemCount)
    }
    override fun getItemCount(): Int {
        return transactions.size
    }
}

