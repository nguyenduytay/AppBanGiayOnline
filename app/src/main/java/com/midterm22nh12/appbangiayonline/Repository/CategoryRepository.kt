package com.midterm22nh12.appbangiayonline.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Category

class CategoryRepository {
    private val database = FirebaseDatabase.getInstance()
    private val categoriesRef = database.getReference("categories")

    fun getCategories(): LiveData<List<Category>> {
        val categoriesLiveData = MutableLiveData<List<Category>>()

        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoriesList = mutableListOf<Category>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    category?.let { categoriesList.add(it) }
                }
                categoriesLiveData.value = categoriesList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return categoriesLiveData
    }

    fun getCategoryById(categoryId: String): MutableLiveData<Category?> {
        val categoryLiveData = MutableLiveData<Category?>()

        categoriesRef.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val category = snapshot.getValue(Category::class.java)
                categoryLiveData.value = category
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return categoryLiveData
    }

    // Thêm loại sản phẩm mới
    fun addCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        categoriesRef.child(category.id).setValue(category)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    // Cập nhật loại sản phẩm
    fun updateCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        categoriesRef.child(category.id).setValue(category)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    // Xóa loại sản phẩm
    fun deleteCategory(categoryId: String, callback: (Boolean, String?) -> Unit) {
        categoriesRef.child(categoryId).removeValue()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }
}