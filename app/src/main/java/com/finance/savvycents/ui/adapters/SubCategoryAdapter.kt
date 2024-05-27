package com.finance.savvycents.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.finance.savvycents.databinding.ItemSubcategoryBinding
import com.finance.savvycents.models.category.SubCategory

class SubCategoryAdapter : ListAdapter<SubCategory, SubCategoryAdapter.SubCategoryViewHolder>(SubCategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val binding = ItemSubcategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SubCategoryViewHolder(private val binding: ItemSubcategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(subCategory: SubCategory) {
            binding.subcategoryName.text = subCategory.name
        }
    }

    class SubCategoryDiffCallback : DiffUtil.ItemCallback<SubCategory>() {
        override fun areItemsTheSame(oldItem: SubCategory, newItem: SubCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubCategory, newItem: SubCategory): Boolean {
            return oldItem == newItem
        }
    }
}
