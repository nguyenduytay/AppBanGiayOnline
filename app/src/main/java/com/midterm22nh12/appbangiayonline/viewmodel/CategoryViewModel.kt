package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.midterm22nh12.appbangiayonline.Repository.CategoryRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Category

class CategoryViewModel : ViewModel() {
    private val repository = CategoryRepository()

    fun getCategories(): LiveData<List<Category>> {
        return repository.getCategories()
    }

    fun getCategoryById(categoryId: String): MutableLiveData<Category?> {
        return repository.getCategoryById(categoryId)
    }

    fun addCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        repository.addCategory(category, callback)
    }

    fun updateCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        repository.updateCategory(category, callback)
    }

    fun deleteCategory(categoryId: String, callback: (Boolean, String?) -> Unit) {
        repository.deleteCategory(categoryId, callback)
    }
}