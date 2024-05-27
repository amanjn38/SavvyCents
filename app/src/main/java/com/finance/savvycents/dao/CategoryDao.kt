package com.finance.savvycents.dao

import androidx.room.*
import com.finance.savvycents.models.category.CategoryEntity
import com.finance.savvycents.models.category.SubCategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId")
    suspend fun getSubCategories(categoryId: String): List<SubCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subcategories: List<SubCategoryEntity>)
}
