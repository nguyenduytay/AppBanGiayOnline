package com.midterm22nh12.appbangiayonline.Repository

import com.midterm22nh12.appbangiayonline.model.Entity.Product
import com.midterm22nh12.appbangiayonline.model.Entity.ProductVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    companion object {
        private const val COLLECTION_PRODUCTS = "products"
    }

    /**
     * Lấy tất cả sản phẩm
     */
    fun getAllProducts(): Flow<List<Product>> = flow {
        val products = firebaseRepository.getAll(COLLECTION_PRODUCTS, Product::class.java)
        emit(products)
    }

    /**
     * Lấy sản phẩm theo ID
     */
    fun getProductById(productId: String): Flow<Product?> = flow {
        val product = firebaseRepository.getById(COLLECTION_PRODUCTS, productId, Product::class.java)
        emit(product)
    }

    /**
     * Lấy sản phẩm theo thương hiệu
     */
    fun getProductsByBrandId(brandId: String): Flow<List<Product>> = flow {
        val products = firebaseRepository.queryByField(
            COLLECTION_PRODUCTS,
            "brandId",
            brandId,
            Product::class.java
        )
        emit(products)
    }

    /**
     * Lấy sản phẩm theo loại thương hiệu
     */
    fun getProductsByCategoryId(categoryId: String): Flow<List<Product>> = flow {
        val products = firebaseRepository.queryByField(
            COLLECTION_PRODUCTS,
            "categoryId",
            categoryId,
            Product::class.java
        )
        emit(products)
    }

    /**
     * Lấy các sản phẩm yêu thích
     */
    fun getFavoriteProducts(): Flow<List<Product>> = flow {
        val products = firebaseRepository.queryByField(
            COLLECTION_PRODUCTS,
            "isFavorite",
            true,
            Product::class.java
        )
        emit(products)
    }

    /**
     * Thêm sản phẩm mới
     */
    suspend fun addProduct(product: Product): String? {
        return firebaseRepository.add(COLLECTION_PRODUCTS, product)
    }

    /**
     * Cập nhật sản phẩm
     */
    suspend fun updateProduct(product: Product): Boolean {
        return firebaseRepository.update(COLLECTION_PRODUCTS, product.id, product)
    }

    /**
     * Xóa sản phẩm
     */
    suspend fun deleteProduct(productId: String): Boolean {
        return firebaseRepository.delete(COLLECTION_PRODUCTS, productId)
    }

    /**
     * Cập nhật trạng thái yêu thích
     */
    suspend fun updateFavoriteStatus(productId: String, isFavorite: Boolean): Boolean {
        val product = firebaseRepository.getById(COLLECTION_PRODUCTS, productId, Product::class.java)
        return if (product != null) {
            val updatedProduct = product.copy(isFavorite = isFavorite)
            firebaseRepository.update(COLLECTION_PRODUCTS, productId, updatedProduct)
        } else {
            false
        }
    }
}