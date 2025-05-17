package com.midterm22nh12.appbangiayonline.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.ProductRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý các thao tác liên quan đến sản phẩm
 * Cung cấp các phương thức để truy xuất, tìm kiếm, lọc và quản lý sản phẩm
 * @param application Application context
 */
class ProductViewModel (application: Application): AndroidViewModel(application) {
    private val repository = ProductRepository()

    /**
     * Lấy danh sách tất cả các sản phẩm
     * @return LiveData<List<Product>> Danh sách sản phẩm được cập nhật tự động khi dữ liệu thay đổi
     */
    fun getProducts(): LiveData<List<Product>> {
        return repository.getProducts()
    }

    /**
     * Lấy thông tin chi tiết của một sản phẩm dựa trên ID
     * @param productId ID của sản phẩm cần lấy thông tin
     * @return MutableLiveData<Product?> Thông tin sản phẩm hoặc null nếu không tìm thấy
     */
    fun getProductById(productId: String): MutableLiveData<Product?> {
        return repository.getProductById(productId)
    }

    /**
     * Lấy danh sách sản phẩm thuộc một danh mục cụ thể
     * @param categoryId ID của danh mục cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm thuộc danh mục
     */
    fun getProductsByCategory(categoryId: String): LiveData<List<Product>> {
        return repository.getProductsByCategory(categoryId)
    }

    /**
     * Lấy danh sách sản phẩm thuộc một thương hiệu cụ thể
     * @param brandId ID của thương hiệu cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm thuộc thương hiệu
     */
    fun getProductsByBrand(brandId: String): LiveData<List<Product>> {
        return repository.getProductsByBrand(brandId)
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     * @param query Chuỗi tìm kiếm cho tên sản phẩm
     * @return LiveData<List<Product>> Danh sách sản phẩm có tên phù hợp
     */
    fun searchProductsByName(query: String): LiveData<List<Product>> {
        return repository.searchProductsByName(query)
    }

    /**
     * Lọc sản phẩm theo khoảng giá
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @return LiveData<List<Product>> Danh sách sản phẩm có giá trong khoảng chỉ định
     */
    fun filterProductsByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Product>> {
        return repository.filterProductsByPriceRange(minPrice, maxPrice)
    }

    /**
     * Lọc sản phẩm theo kích cỡ có sẵn
     * @param size Kích cỡ cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm có kích cỡ yêu cầu và trạng thái "available"
     */
    fun filterProductsByAvailableSize(size: String): LiveData<List<Product>> {
        return repository.filterProductsByAvailableSize(size)
    }

    /**
     * Lọc sản phẩm theo màu sắc có sẵn
     * @param colorName Tên màu cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm có màu yêu cầu, trạng thái "available" và còn trong kho
     */
    fun filterProductsByAvailableColor(colorName: String): LiveData<List<Product>> {
        return repository.filterProductsByAvailableColor(colorName)
    }

    /**
     * Thêm sản phẩm mới vào database
     * @param product Đối tượng Product chứa thông tin sản phẩm mới
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun addProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        repository.addProduct(product, callback)
    }

    /**
     * Cập nhật thông tin sản phẩm
     * @param product Đối tượng Product chứa thông tin cập nhật
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun updateProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        repository.updateProduct(product, callback)
    }

    /**
     * Xóa một sản phẩm khỏi database
     * @param productId ID của sản phẩm cần xóa
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun deleteProduct(productId: String, callback: (Boolean, String?) -> Unit) {
        repository.deleteProduct(productId, callback)
    }

    /**
     * Tìm kiếm sản phẩm với nhiều tiêu chí kết hợp
     * Tìm kiếm tên sản phẩm không phân biệt hoa thường
     *
     * @param categoryId ID của loại sản phẩm (null nếu không sử dụng tiêu chí này)
     * @param brandId ID của thương hiệu (null nếu không sử dụng tiêu chí này)
     * @param nameQuery Chuỗi con trong tên sản phẩm (null nếu không sử dụng tiêu chí này)
     * @return LiveData<List<Product>> Danh sách sản phẩm phù hợp với tất cả các tiêu chí được chỉ định
     */
    fun searchProducts(
        categoryId: String? = null,
        brandId: String? = null,
        nameQuery: String? = null
    ): LiveData<List<Product>> {
        return repository.searchProductsWithMultipleCriteria(categoryId, brandId, nameQuery)
    }

    /**
     * Lấy thông tin chi tiết sản phẩm từ một ItemRecyclerViewConfirmation
     * Chuyển đổi dữ liệu từ xác nhận đơn hàng sang định dạng hiển thị chi tiết sản phẩm
     * Sử dụng coroutines để thực hiện thao tác bất đồng bộ
     *
     * @param confirmation Thông tin xác nhận đơn hàng cần chuyển đổi
     * @param onSuccess Callback khi lấy thông tin thành công, nhận ItemRecyclerViewProductHomeUser làm tham số
     * @param onError Callback khi xảy ra lỗi, nhận thông báo lỗi làm tham số
     */
    fun getProductDetailFromConfirmation(
        confirmation: ItemRecyclerViewConfirmation,
        onSuccess: (ItemRecyclerViewProductHomeUser) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val productDetail = repository.getProductHomeUserFromConfirmation(confirmation)

                if (productDetail != null) {
                    // Gọi callback thành công với dữ liệu sản phẩm
                    onSuccess(productDetail)
                } else {
                    // Gọi callback lỗi nếu không tìm thấy sản phẩm
                    onError("Không tìm thấy thông tin sản phẩm")
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Lỗi khi lấy thông tin sản phẩm: ${e.message}")
                // Gọi callback lỗi với thông báo lỗi
                onError(e.message ?: "Đã xảy ra lỗi khi lấy thông tin sản phẩm")
            }
        }
    }
}