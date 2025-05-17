package com.midterm22nh12.appbangiayonline.model.Entity.Product

data class Product(
    val id: String = "",                // Mã định danh duy nhất của sản phẩm
    val brandId: String = "",           // Mã định danh của thương hiệu sản phẩm
    val categoryId: String = "",        // Mã định danh của danh mục sản phẩm
    val name: String = "",              // Tên sản phẩm
    val price: Long = 0,                // Giá cơ bản của sản phẩm
    val rating: Double = 0.0,           // Điểm đánh giá trung bình của sản phẩm
    val description: String = "",       // Mô tả chi tiết về sản phẩm
    val sizes: List<ProductSize> = emptyList(),     // Danh sách các kích cỡ có sẵn
    val colors: List<ProductColor> = emptyList()    // Danh sách các màu sắc có sẵn
)