package com.midterm22nh12.appbangiayonline.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand

class BrandRepository {
    private val database = FirebaseDatabase.getInstance()
    private val brandsRef = database.getReference("brands")

    fun getBrands(): LiveData<List<Brand>> {
        val brandsLiveData = MutableLiveData<List<Brand>>()

        brandsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val brandsList = mutableListOf<Brand>()
                for (brandSnapshot in snapshot.children) {
                    val brand = brandSnapshot.getValue(Brand::class.java)
                    brand?.let { brandsList.add(it) }
                }
                brandsLiveData.value = brandsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return brandsLiveData
    }

    fun getBrandById(brandId: String): MutableLiveData<Brand?> {
        val brandLiveData = MutableLiveData<Brand?>()

        brandsRef.child(brandId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val brand = snapshot.getValue(Brand::class.java)
                brandLiveData.value = brand
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return brandLiveData
    }

    // Thêm thương hiệu mới
    fun addBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        brandsRef.child(brand.id).setValue(brand)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    // Cập nhật thương hiệu
    fun updateBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        brandsRef.child(brand.id).setValue(brand)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    // Xóa thương hiệu
    fun deleteBrand(brandId: String, callback: (Boolean, String?) -> Unit) {
        brandsRef.child(brandId).removeValue()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }
}