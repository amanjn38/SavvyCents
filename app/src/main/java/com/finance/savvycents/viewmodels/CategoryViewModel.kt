package com.finance.savvycents.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.savvycents.models.category.Category
import com.finance.savvycents.repository.CategoryRepository
import com.finance.savvycents.utilities.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(private val repository: CategoryRepository) : ViewModel() {

    private val _categories = MutableLiveData<Resource<List<Category>>>()
    val categories: LiveData<Resource<List<Category>>> = _categories

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        _categories.postValue(Resource.Loading())
        viewModelScope.launch {
            val result = repository.getCategories()
            _categories.postValue(result)
        }
    }
}