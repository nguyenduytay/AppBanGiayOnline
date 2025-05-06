package com.midterm22nh12.appbangiayonline.model.Entity

data class ProductVariant(
    val id: String = "",
    val productId: String = "", // Tham chiếu đến sản phẩm
    val name: String = "",
    val color: String = "",
    val imageUrl: String = ""
)