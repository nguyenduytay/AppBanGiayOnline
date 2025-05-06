package com.midterm22nh12.appbangiayonline.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.BrandRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Brand
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BrandViewModel(
    private val repository: BrandRepository = BrandRepository()
) : ViewModel() {

    // LiveData cho danh sách thương hiệu
    private val _brands = MutableLiveData<List<Brand>>()
    val brands: LiveData<List<Brand>> get() = _brands

    // LiveData cho thương hiệu đang được chọn
    private val _selectedBrand = MutableLiveData<Brand?>()
    val selectedBrand: LiveData<Brand?> get() = _selectedBrand

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData cho thông báo lỗi
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Lấy tất cả thương hiệu
    fun loadAllBrands() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllBrands()
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { brandList ->
                    _brands.value = brandList
                    _isLoading.value = false
                }
        }
    }

    // Lấy thương hiệu theo ID
    fun loadBrandById(brandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getBrandById(brandId)
                .catch { e ->
                    _errorMessage.value = e.message
                    _isLoading.value = false
                }
                .collect { brand ->
                    _selectedBrand.value = brand
                    _isLoading.value = false
                }
        }
    }

    // Thêm thương hiệu mới
    fun addBrand(name: String, imageUrl: String) {
        val newBrand = Brand(
            id = "",  // ID sẽ được Firebase tạo ra
            name = name,
            imageUrl = imageUrl
        )

        viewModelScope.launch {
            _isLoading.value = true
            val brandId = repository.addBrand(newBrand)
            if (brandId != null) {
                // Thêm thành công, làm mới danh sách
                loadAllBrands()
            } else {
                _errorMessage.value = "Không thể thêm thương hiệu mới"
                _isLoading.value = false
            }
        }
    }

    // Cập nhật thương hiệu
    fun updateBrand(brand: Brand) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.updateBrand(brand)
            if (success) {
                // Cập nhật thành công, làm mới danh sách
                loadAllBrands()
                _selectedBrand.value = brand
            } else {
                _errorMessage.value = "Không thể cập nhật thương hiệu"
                _isLoading.value = false
            }
        }
    }

    // Xóa thương hiệu
    fun deleteBrand(brandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.deleteBrand(brandId)
            if (success) {
                // Xóa thành công, làm mới danh sách
                loadAllBrands()
                if (_selectedBrand.value?.id == brandId) {
                    _selectedBrand.value = null
                }
            } else {
                _errorMessage.value = "Không thể xóa thương hiệu"
                _isLoading.value = false
            }
        }
    }

    // Xóa thông báo lỗi
    fun clearError() {
        _errorMessage.value = null
    }
}