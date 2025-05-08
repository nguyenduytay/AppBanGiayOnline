package com.midterm22nh12.appbangiayonline.Repository

import com.midterm22nh12.appbangiayonline.model.Entity.Brand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrandRepository(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    companion object {
        private const val COLLECTION_BRANDS = "brands"
    }

    /**
     * Lấy tất cả thương hiệu
     */
    fun getAllBrands(): Flow<List<Brand>> = flow {
        val brands = firebaseRepository.getAll(COLLECTION_BRANDS, Brand::class.java)
        emit(brands)
    }

    /**
     * Lấy thương hiệu theo ID
     */
    fun getBrandById(brandId: String): Flow<Brand?> = flow {
        val brand = firebaseRepository.getById(COLLECTION_BRANDS, brandId, Brand::class.java)
        emit(brand)
    }

    /**
     * Thêm thương hiệu mới
     */
    suspend fun addBrand(brand: Brand): String? {
        return firebaseRepository.add(COLLECTION_BRANDS, brand)
    }

    /**
     * Cập nhật thương hiệu
     */
    suspend fun updateBrand(brand: Brand): Boolean {
        return firebaseRepository.update(COLLECTION_BRANDS, brand.id, brand)
    }

    /**
     * Xóa thương hiệu
     */
    suspend fun deleteBrand(brandId: String): Boolean {
        return firebaseRepository.delete(COLLECTION_BRANDS, brandId)
    }
}