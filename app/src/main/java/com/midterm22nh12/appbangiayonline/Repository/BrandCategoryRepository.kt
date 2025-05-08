package com.midterm22nh12.appbangiayonline.Repository

import com.midterm22nh12.appbangiayonline.model.Entity.BrandCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrandCategoryRepository(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    companion object {
        private const val COLLECTION_BRAND_CATEGORIES = "brand_categories"
    }

    /**
     * Lấy tất cả loại sản phẩm thương hiệu
     */
    fun getAllBrandCategories(): Flow<List<BrandCategory>> = flow {
        val categories = firebaseRepository.getAll(COLLECTION_BRAND_CATEGORIES, BrandCategory::class.java)
        emit(categories)
    }

    /**
     * Lấy loại sản phẩm thương hiệu theo ID
     */
    fun getBrandCategoryById(categoryId: String): Flow<BrandCategory?> = flow {
        val category = firebaseRepository.getById(COLLECTION_BRAND_CATEGORIES, categoryId, BrandCategory::class.java)
        emit(category)
    }

    /**
     * Lấy các loại sản phẩm của một thương hiệu
     */
    fun getCategoriesByBrandId(brandId: String): Flow<List<BrandCategory>> = flow {
        val categories = firebaseRepository.queryByField(
            COLLECTION_BRAND_CATEGORIES,
            "brandId",
            brandId,
            BrandCategory::class.java
        )
        emit(categories)
    }

    /**
     * Thêm loại sản phẩm thương hiệu mới
     */
    suspend fun addBrandCategory(category: BrandCategory): String? {
        return firebaseRepository.add(COLLECTION_BRAND_CATEGORIES, category)
    }

    /**
     * Cập nhật loại sản phẩm thương hiệu
     */
    suspend fun updateBrandCategory(category: BrandCategory): Boolean {
        return firebaseRepository.update(COLLECTION_BRAND_CATEGORIES, category.id, category)
    }

    /**
     * Xóa loại sản phẩm thương hiệu
     */
    suspend fun deleteBrandCategory(categoryId: String): Boolean {
        return firebaseRepository.delete(COLLECTION_BRAND_CATEGORIES, categoryId)
    }
}