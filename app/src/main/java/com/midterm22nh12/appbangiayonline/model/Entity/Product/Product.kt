package com.midterm22nh12.appbangiayonline.model.Entity.Product

data class Product(
    val id: String = "",
    val brandId: String = "",
    val categoryId: String = "",
    val name: String = "",
    val price: Long = 0,
    val rating: Double = 0.0,
    val description: String = "",
    val sizes: List<ProductSize> = emptyList(),
    val colors: List<ProductColor> = emptyList()
)