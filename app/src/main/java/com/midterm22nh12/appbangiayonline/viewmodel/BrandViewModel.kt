package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.midterm22nh12.appbangiayonline.Repository.BrandRepository
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand

class BrandViewModel : ViewModel() {
    private val repository = BrandRepository()

    fun getBrands(): LiveData<List<Brand>> {
        return repository.getBrands()
    }

    fun getBrandById(brandId: String): MutableLiveData<Brand?> {
        return repository.getBrandById(brandId)
    }

    fun addBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        repository.addBrand(brand, callback)
    }

    fun updateBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        repository.updateBrand(brand, callback)
    }

    fun deleteBrand(brandId: String, callback: (Boolean, String?) -> Unit) {
        repository.deleteBrand(brandId, callback)
    }
}