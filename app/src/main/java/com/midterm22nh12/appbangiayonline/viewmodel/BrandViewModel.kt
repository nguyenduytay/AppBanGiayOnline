package com.midterm22nh12.appbangiayonline.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.midterm22nh12.appbangiayonline.Repository.BrandRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand

/**
 * ViewModel để quản lý và xử lý các thao tác liên quan đến thương hiệu (Brand)
 * Kế thừa từ AndroidViewModel để có thể truy cập Application context
 * @param application Application context
 */
class BrandViewModel (application: Application): AndroidViewModel(application) {
    private val repository = BrandRepository()

    /**
     * Lấy danh sách tất cả các thương hiệu
     * @return LiveData<List<Brand>> Danh sách thương hiệu được cập nhật tự động khi dữ liệu thay đổi
     */
    fun getBrands(): LiveData<List<Brand>> {
        return repository.getBrands()
    }

    /**
     * Lấy thông tin chi tiết của một thương hiệu dựa trên ID
     * @param brandId ID của thương hiệu cần lấy thông tin
     * @return MutableLiveData<Brand?> Thông tin thương hiệu hoặc null nếu không tìm thấy
     */
    fun getBrandById(brandId: String): MutableLiveData<Brand?> {
        return repository.getBrandById(brandId)
    }

    /**
     * Thêm thương hiệu mới vào database
     * @param brand Đối tượng Brand chứa thông tin thương hiệu mới
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun addBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        repository.addBrand(brand, callback)
    }

    /**
     * Cập nhật thông tin thương hiệu
     * @param brand Đối tượng Brand chứa thông tin cập nhật
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun updateBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        repository.updateBrand(brand, callback)
    }

    /**
     * Xóa một thương hiệu khỏi database
     * @param brandId ID của thương hiệu cần xóa
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun deleteBrand(brandId: String, callback: (Boolean, String?) -> Unit) {
        repository.deleteBrand(brandId, callback)
    }
}