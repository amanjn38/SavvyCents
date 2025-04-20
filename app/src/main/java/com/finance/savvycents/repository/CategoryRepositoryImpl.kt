package com.finance.savvycents.repository

import com.finance.savvycents.dao.CategoryDao
import com.finance.savvycents.models.category.Category
import com.finance.savvycents.models.category.CategoryEntity
import com.finance.savvycents.models.category.SubCategory
import com.finance.savvycents.models.category.SubCategoryEntity
import com.finance.savvycents.utilities.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getCategories(): Resource<List<Category>> {
        return try {
            val categories = categoryDao.getAllCategories()
            if (categories.isNotEmpty()) {
                val categoryList = categories.map { categoryEntity ->
                    val subcategories = categoryDao.getSubCategories(categoryEntity.id)
                    Category(
                        id = categoryEntity.id,
                        name = categoryEntity.name,
                        subcategories = subcategories.map { SubCategory(it.id, it.name) }
                    )
                }
                Resource.Success(categoryList)
            } else {
                fetchCategoriesFromFirestore()
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown Error")
        }
    }

    private suspend fun fetchCategoriesFromFirestore(): Resource<List<Category>> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            val categories = snapshot.documents.map { document ->
                val subcategories = document.reference.collection("subcategories").get().await()
                    .documents.map { subDoc ->
                        SubCategory(
                            id = subDoc.id,
                            name = subDoc.getString("name") ?: ""
                        )
                    }
                Category(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    subcategories = subcategories
                )
            }
            saveCategoriesToRoom(categories)
            Resource.Success(categories)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch categories from Firestore")
        }
    }

    private suspend fun saveCategoriesToRoom(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { CategoryEntity(it.id, it.name) })
        categories.forEach { category ->
            categoryDao.insertSubCategories(category.subcategories.map {
                SubCategoryEntity(it.id, category.id, it.name)
            })
        }
    }
}

