package com.finance.savvycents.models.category

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcategories")
data class SubCategoryEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String
)