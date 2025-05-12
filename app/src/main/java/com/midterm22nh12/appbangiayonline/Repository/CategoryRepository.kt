package com.midterm22nh12.appbangiayonline.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Category

/**
 * Repository xử lý các hoạt động liên quan đến danh mục sản phẩm
 */
class CategoryRepository {
    private val database = FirebaseDatabase.getInstance()
    private val categoriesRef = database.getReference("categories")

    /**
     * Lấy danh sách tất cả danh mục
     * @return LiveData chứa danh sách danh mục
     */
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

    /**
     * Lấy thông tin danh mục theo ID
     * @param categoryId ID của danh mục
     * @return LiveData chứa thông tin danh mục
     */
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

    /**
     * Thêm danh mục mới
     * @param category Đối tượng danh mục cần thêm
     * @param callback Callback xử lý kết quả
     */
    fun addCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        categoriesRef.child(category.id).setValue(category)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Cập nhật thông tin danh mục
     * @param category Đối tượng danh mục cần cập nhật
     * @param callback Callback xử lý kết quả
     */
    fun updateCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        categoriesRef.child(category.id).setValue(category)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Xóa danh mục
     * @param categoryId ID của danh mục cần xóa
     * @param callback Callback xử lý kết quả
     */
    fun deleteCategory(categoryId: String, callback: (Boolean, String?) -> Unit) {
        categoriesRef.child(categoryId).removeValue()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Lấy danh sách danh mục theo trạng thái
     * @param active Trạng thái hoạt động cần lọc (true/false)
     * @return LiveData chứa danh sách danh mục đã lọc
     */
    fun getCategoriesByStatus(active: Boolean): LiveData<List<Category>> {
        val categoriesLiveData = MutableLiveData<List<Category>>()

        categoriesRef.orderByChild("active").equalTo(active).addValueEventListener(object : ValueEventListener {
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
}