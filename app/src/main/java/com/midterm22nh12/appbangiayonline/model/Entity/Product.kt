package com.midterm22nh12.appbangiayonline.model.Entity

data class Product(
    val id: String = "",
    val price: Double = 0.0,
    val isFavorite: Boolean = false,
    val rating: Float = 0f, // Số sao đánh giá
    val brandId: String = "", // Tham chiếu đến thương hiệu
    val categoryId: String = "" // Tham chiếu đến loại sản phẩm thương hiệu
)