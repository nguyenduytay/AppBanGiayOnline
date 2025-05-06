package com.midterm22nh12.appbangiayonline.Repository



import com.midterm22nh12.appbangiayonline.model.Entity.ProductVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductVariantRepository(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    companion object {
        private const val COLLECTION_PRODUCT_VARIANTS = "product_variants"
    }

    /**
     * Lấy tất cả biến thể sản phẩm
     */
    fun getAllProductVariants(): Flow<List<ProductVariant>> = flow {
        val variants = firebaseRepository.getAll(COLLECTION_PRODUCT_VARIANTS, ProductVariant::class.java)
        emit(variants)
    }

    /**
     * Lấy biến thể sản phẩm theo ID
     */
    fun getProductVariantById(variantId: String): Flow<ProductVariant?> = flow {
        val variant = firebaseRepository.getById(COLLECTION_PRODUCT_VARIANTS, variantId, ProductVariant::class.java)
        emit(variant)
    }

    /**
     * Lấy các biến thể của một sản phẩm
     */
    fun getVariantsByProductId(productId: String): Flow<List<ProductVariant>> = flow {
        val variants = firebaseRepository.queryByField(
            COLLECTION_PRODUCT_VARIANTS,
            "productId",
            productId,
            ProductVariant::class.java
        )
        emit(variants)
    }

    /**
     * Thêm biến thể sản phẩm mới
     */
    suspend fun addProductVariant(variant: ProductVariant): String? {
        return firebaseRepository.add(COLLECTION_PRODUCT_VARIANTS, variant)
    }

    /**
     * Cập nhật biến thể sản phẩm
     */
    suspend fun updateProductVariant(variant: ProductVariant): Boolean {
        return firebaseRepository.update(COLLECTION_PRODUCT_VARIANTS, variant.id, variant)
    }

    /**
     * Xóa biến thể sản phẩm
     */
    suspend fun deleteProductVariant(variantId: String): Boolean {
        return firebaseRepository.delete(COLLECTION_PRODUCT_VARIANTS, variantId)
    }
}