package com.finance.savvycents.ui.adapters

import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.R
import com.finance.savvycents.models.Transaction
import com.finance.savvycents.utilities.DataDiffCallback
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions = mutableListOf<Transaction>()

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val dateTimeTextView: TextView = itemView.findViewById(R.id.dateTimeTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        val subCategoryTextView: TextView = itemView.findViewById(R.id.subCategoryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val model = transactions[position]
        // Bind data to views
        holder.typeTextView.text = "Type: ${model.type}"
        holder.locationTextView.text = "Location: ${model.location}"
        holder.dateTimeTextView.text = "Date & Time: ${model.dateTime}"
        holder.amountTextView.text = "Amount: ${model.amount}"
        holder.categoryTextView.text = "Category: ${model.category}"
        holder.subCategoryTextView.text = "Sub Category: ${model.subCategory}"
    }

    fun submitList(newList: List<Transaction>) {
        DiffUtil.calculateDiff(DataDiffCallback(transactions, newList)).dispatchUpdatesTo(this)
        transactions.clear()
        transactions.addAll(newList)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
    }

