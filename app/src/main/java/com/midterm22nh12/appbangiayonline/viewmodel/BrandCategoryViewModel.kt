package com.midterm22nh12.appbangiayonline.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.BrandCategoryRepository
import com.midterm22nh12.appbangiayonline.model.Entity.BrandCategory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BrandCategoryViewModel(
    private val repository: BrandCategoryRepository = BrandCategoryRepository()
) : ViewModel() {

    // LiveData cho danh sách loại sản phẩm
    private val _categories = MutableLiveData<List<BrandCategory>>()
    val categories: LiveData<List<BrandCategory>> get() = _categories

    // LiveData cho loại sản phẩm đang được chọn
    private val _selectedCategory = MutableLiveData<BrandCategory?>()
    val selectedCategory: LiveData<BrandCategory?> get() = _selectedCategory

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData cho thông báo lỗi
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Lấy tất cả loại sản phẩm
    fun loadAllCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllBrandCategories()
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { categoryList ->
                    _categories.value = categoryList
                    _isLoading.value = false
                }
        }
    }

    // Lấy loại sản phẩm theo ID
    fun loadCategoryById(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getBrandCategoryById(categoryId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { category ->
                    _selectedCategory.value = category
                    _isLoading.value = false
                }
        }
    }

    // Lấy các loại sản phẩm theo thương hiệu
    fun loadCategoriesByBrandId(brandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCategoriesByBrandId(brandId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { categoryList ->
                    _categories.value = categoryList
                    _isLoading.value = false
                }
        }
    }

    // Thêm loại sản phẩm mới
    fun addCategory(name: String, brandId: String) {
        val newCategory = BrandCategory(
            id = "",  // ID sẽ được Firebase tạo ra
            name = name,
            brandId = brandId
        )

        viewModelScope.launch {
            _isLoading.value = true
            val categoryId = repository.addBrandCategory(newCategory)
            if (categoryId != null) {
                // Thêm thành công, làm mới danh sách
                loadCategoriesByBrandId(brandId)
            } else {
                _errorMessage.value = "Không thể thêm loại sản phẩm mới"
                _isLoading.value = false
            }
        }
    }

    // Cập nhật loại sản phẩm
    fun updateCategory(category: BrandCategory) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateBrandCategory(category)
            if (success) {
                // Cập nhật thành công, làm mới danh sách
                loadCategoriesByBrandId(category.brandId)
                _selectedCategory.value = category
            } else {
                _errorMessage.value = "Không thể cập nhật loại sản phẩm"
                _isLoading.value = false
            }
        }
    }

    // Xóa loại sản phẩm
    fun deleteCategory(categoryId: String, brandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteBrandCategory(categoryId)
            if (success) {
                // Xóa thành công, làm mới danh sách
                loadCategoriesByBrandId(brandId)
                if (_selectedCategory.value?.id == categoryId) {
                    _selectedCategory.value = null
                }
            } else {
                _errorMessage.value = "Không thể xóa loại sản phẩm"
                _isLoading.value = false
            }
        }
    }

    // Xóa thông báo lỗi
    fun clearError() {
        _errorMessage.value = null
    }
}