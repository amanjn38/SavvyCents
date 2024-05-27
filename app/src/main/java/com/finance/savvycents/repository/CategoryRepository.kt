package com.finance.savvycents.repository

import com.finance.savvycents.models.category.Category
import com.finance.savvycents.utilities.Resource

interface CategoryRepository {
    suspend fun getCategories(): Resource<List<Category>>
}
