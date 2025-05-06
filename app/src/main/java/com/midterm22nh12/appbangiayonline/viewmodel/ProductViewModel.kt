package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.ProductRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Product
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    // LiveData cho danh sách sản phẩm
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    // LiveData cho sản phẩm đang được chọn
    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> get() = _selectedProduct

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData cho thông báo lỗi
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Lấy tất cả sản phẩm
    fun loadAllProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllProducts()
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                }
        }
    }

    // Lấy sản phẩm theo ID
    fun loadProductById(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductById(productId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { product ->
                    _selectedProduct.value = product
                    _isLoading.value = false
                }
        }
    }

    // Lấy sản phẩm theo thương hiệu
    fun loadProductsByBrandId(brandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductsByBrandId(brandId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                }
        }
    }

    // Lấy sản phẩm theo loại sản phẩm
    fun loadProductsByCategoryId(categoryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductsByCategoryId(categoryId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                }
        }
    }

    // Lấy sản phẩm yêu thích
    fun loadFavoriteProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getFavoriteProducts()
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { productList ->
                    _products.value = productList
                    _isLoading.value = false
                }
        }
    }

    // Thêm sản phẩm mới
    fun addProduct(price: Double, brandId: String, categoryId: String) {
        val newProduct = Product(
            id = "",  // ID sẽ được Firebase tạo ra
            price = price,
            isFavorite = false,
            rating = 0f,
            brandId = brandId,
            categoryId = categoryId
        )

        viewModelScope.launch {
            _isLoading.value = true
            val productId = repository.addProduct(newProduct)
            if (productId != null) {
                // Thêm thành công, làm mới danh sách
                loadProductsByCategoryId(categoryId)
            } else {
                _errorMessage.value = "Không thể thêm sản phẩm mới"
                _isLoading.value = false
            }
        }
    }

    // Cập nhật sản phẩm
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateProduct(product)
            if (success) {
                // Cập nhật thành công, làm mới danh sách và cập nhật sản phẩm đang chọn
                when {
                    product.isFavorite -> loadFavoriteProducts()
                    else -> loadProductsByCategoryId(product.categoryId)
                }
                _selectedProduct.value = product
            } else {
                _errorMessage.value = "Không thể cập nhật sản phẩm"
                _isLoading.value = false
            }
        }
    }
    // Cập nhật trạng thái yêu thích
    fun updateFavoriteStatus(productId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateFavoriteStatus(productId, isFavorite)
            if (success) {
                // Cập nhật thành công, làm mới sản phẩm đang chọn
                loadProductById(productId)

                // Nếu đang ở màn hình yêu thích, cập nhật lại danh sách
                if (_products.value?.any { it.isFavorite } == true) {
                    loadFavoriteProducts()
                }
            } else {
                _errorMessage.value = "Không thể cập nhật trạng thái yêu thích"
                _isLoading.value = false
            }
        }
    }

    // Xóa sản phẩm
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteProduct(productId)
            if (success) {
                // Xóa thành công, làm mới danh sách
                _selectedProduct.value?.let { product ->
                    loadProductsByCategoryId(product.categoryId)
                } ?: loadAllProducts()

                if (_selectedProduct.value?.id == productId) {
                    _selectedProduct.value = null
                }
            } else {
                _errorMessage.value = "Không thể xóa sản phẩm"
                _isLoading.value = false
            }
        }
    }

    // Xóa thông báo lỗi
    fun clearError() {
        _errorMessage.value = null
    }
}