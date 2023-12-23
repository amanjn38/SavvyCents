package com.finance.savvycents.utilities

import androidx.recyclerview.widget.DiffUtil
import com.finance.savvycents.models.Transaction


class DataDiffCallback(
    private val oldList: List<Transaction>,
    private val newList: List<Transaction>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // If you have more complex items, you can implement a more precise equality check here
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
