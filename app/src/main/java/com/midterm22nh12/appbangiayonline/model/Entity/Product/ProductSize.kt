package com.midterm22nh12.appbangiayonline.model.Entity.Product

// Size class with status
data class ProductSize(
    val value: String = "",         // Giá trị kích thước (ví dụ: "38", "39", "40", "M", "L")
    val status: String = "available" // Trạng thái kích thước: có sẵn, ẩn, hết hàng, sắp có
)