package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.midterm22nh12.appbangiayonline.Repository.ProductRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    fun getProducts(): LiveData<List<Product>> {
        return repository.getProducts()
    }

    fun getProductById(productId: String): MutableLiveData<Product?> {
        return repository.getProductById(productId)
    }

    fun getProductsByCategory(categoryId: String): LiveData<List<Product>> {
        return repository.getProductsByCategory(categoryId)
    }

    fun getProductsByBrand(brandId: String): LiveData<List<Product>> {
        return repository.getProductsByBrand(brandId)
    }

    fun searchProductsByName(query: String): LiveData<List<Product>> {
        return repository.searchProductsByName(query)
    }

    fun filterProductsByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Product>> {
        return repository.filterProductsByPriceRange(minPrice, maxPrice)
    }

    // Add new filtering methods
    fun filterProductsByAvailableSize(size: String): LiveData<List<Product>> {
        return repository.filterProductsByAvailableSize(size)
    }

    fun filterProductsByAvailableColor(colorName: String): LiveData<List<Product>> {
        return repository.filterProductsByAvailableColor(colorName)
    }

    fun addProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        repository.addProduct(product, callback)
    }

    fun updateProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        repository.updateProduct(product, callback)
    }

    fun deleteProduct(productId: String, callback: (Boolean, String?) -> Unit) {
        repository.deleteProduct(productId, callback)
    }
    /**
     * Tìm kiếm sản phẩm với nhiều tiêu chí kết hợp
     * Tìm kiếm tên sản phẩm không phân biệt hoa thường
     */
    fun searchProducts(
        categoryId: String? = null,
        brandId: String? = null,
        nameQuery: String? = null
    ): LiveData<List<Product>> {
        return repository.searchProductsWithMultipleCriteria(categoryId, brandId, nameQuery)
    }
}