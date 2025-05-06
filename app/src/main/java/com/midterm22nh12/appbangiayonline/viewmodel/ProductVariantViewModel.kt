package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.ProductVariantRepository
import com.midterm22nh12.appbangiayonline.model.Entity.ProductVariant
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProductVariantViewModel(
    private val repository: ProductVariantRepository = ProductVariantRepository()
) : ViewModel() {

    // LiveData cho danh sách biến thể sản phẩm
    private val _variants = MutableLiveData<List<ProductVariant>>()
    val variants: LiveData<List<ProductVariant>> get() = _variants

    // LiveData cho biến thể đang được chọn
    private val _selectedVariant = MutableLiveData<ProductVariant?>()
    val selectedVariant: LiveData<ProductVariant?> get() = _selectedVariant

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData cho thông báo lỗi
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Lấy tất cả biến thể sản phẩm
    fun loadAllVariants() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllProductVariants()
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { variantList ->
                    _variants.value = variantList
                    _isLoading.value = false
                }
        }
    }

    // Lấy biến thể theo ID
    fun loadVariantById(variantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductVariantById(variantId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { variant ->
                    _selectedVariant.value = variant
                    _isLoading.value = false
                }
        }
    }

    // Lấy các biến thể của một sản phẩm
    fun loadVariantsByProductId(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getVariantsByProductId(productId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { variantList ->
                    _variants.value = variantList
                    _isLoading.value = false
                }
        }
    }

    // Thêm biến thể sản phẩm mới
    fun addVariant(productId: String, name: String, color: String, imageUrl: String) {
        val newVariant = ProductVariant(
            id = "",  // ID sẽ được Firebase tạo ra
            productId = productId,
            name = name,
            color = color,
            imageUrl = imageUrl
        )

        viewModelScope.launch {
            _isLoading.value = true
            val variantId = repository.addProductVariant(newVariant)
            if (variantId != null) {
                // Thêm thành công, làm mới danh sách
                loadVariantsByProductId(productId)
            } else {
                _errorMessage.value = "Không thể thêm biến thể sản phẩm mới"
                _isLoading.value = false
            }
        }
    }

    // Cập nhật biến thể sản phẩm
    fun updateVariant(variant: ProductVariant) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateProductVariant(variant)
            if (success) {
                // Cập nhật thành công, làm mới danh sách
                loadVariantsByProductId(variant.productId)
                _selectedVariant.value = variant
            } else {
                _errorMessage.value = "Không thể cập nhật biến thể sản phẩm"
                _isLoading.value = false
            }
        }
    }

    // Xóa biến thể sản phẩm
    fun deleteVariant(variantId: String, productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteProductVariant(variantId)
            if (success) {
                // Xóa thành công, làm mới danh sách
                loadVariantsByProductId(productId)
                if (_selectedVariant.value?.id == variantId) {
                    _selectedVariant.value = null
                }
            } else {
                _errorMessage.value = "Không thể xóa biến thể sản phẩm"
                _isLoading.value = false
            }
        }
    }

    // Xóa thông báo lỗi
    fun clearError() {
        _errorMessage.value = null
    }
}