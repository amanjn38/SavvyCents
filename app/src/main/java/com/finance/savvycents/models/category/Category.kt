package com.finance.savvycents.models.category

data class Category(
    val id: String,
    val name: String,
    val subcategories: List<SubCategory>
)