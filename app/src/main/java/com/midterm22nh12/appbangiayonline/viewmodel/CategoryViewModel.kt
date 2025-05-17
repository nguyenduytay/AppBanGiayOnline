package com.midterm22nh12.appbangiayonline.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.CategoryRepository
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Category
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel xử lý các logic liên quan đến danh mục sản phẩm
 */
class CategoryViewModel (application: Application): AndroidViewModel(application) {
    private val repository = CategoryRepository()

    // UI State cho thao tác thêm/sửa/xóa
    private val _operationState = MutableLiveData<UiState<String>?>()
    val operationState: MutableLiveData<UiState<String>?> = _operationState

    /**
     * Lấy danh sách tất cả danh mục
     * @return LiveData chứa danh sách danh mục
     */
    fun getCategories(): LiveData<List<Category>> {
        return repository.getCategories()
    }

    /**
     * Lấy thông tin danh mục theo ID
     * @param categoryId ID của danh mục
     * @return LiveData chứa thông tin danh mục
     */
    fun getCategoryById(categoryId: String): LiveData<Category?> {
        return repository.getCategoryById(categoryId)
    }

    /**
     * Thêm danh mục mới
     * @param name Tên danh mục
     */
    fun addCategory(name: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            val categoryId = UUID.randomUUID().toString()
            val newCategory = Category(id = categoryId, name = name)

            repository.addCategory(newCategory) { success, errorMessage ->
                if (success) {
                    _operationState.value = UiState.Success("Thêm danh mục thành công")
                } else {
                    _operationState.value = UiState.Error(errorMessage ?: "Không thể thêm danh mục")
                }
            }
        }
    }

    /**
     * Cập nhật thông tin danh mục
     * @param category Đối tượng danh mục cần cập nhật
     */
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            repository.updateCategory(category) { success, errorMessage ->
                if (success) {
                    _operationState.value = UiState.Success("Cập nhật danh mục thành công")
                } else {
                    _operationState.value = UiState.Error(errorMessage ?: "Không thể cập nhật danh mục")
                }
            }
        }
    }

    /**
     * Xóa danh mục
     * @param categoryId ID của danh mục cần xóa
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading

            repository.deleteCategory(categoryId) { success, errorMessage ->
                if (success) {
                    _operationState.value = UiState.Success("Xóa danh mục thành công")
                } else {
                    _operationState.value = UiState.Error(errorMessage ?: "Không thể xóa danh mục")
                }
            }
        }
    }

    /**
     * Lấy danh sách danh mục theo trạng thái
     * @param active Trạng thái hoạt động cần lọc (true/false)
     * @return LiveData chứa danh sách danh mục đã lọc
     */
    fun getCategoriesByStatus(active: Boolean): LiveData<List<Category>> {
        return repository.getCategoriesByStatus(active)
    }

    /**
     * Đặt lại trạng thái UI sau khi hoàn thành thao tác
     */
    fun resetOperationState() {
        _operationState.value = null
    }
}