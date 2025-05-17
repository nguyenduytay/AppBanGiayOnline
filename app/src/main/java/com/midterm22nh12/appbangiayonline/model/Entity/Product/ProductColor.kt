package com.midterm22nh12.appbangiayonline.model.Entity.Product

// Color class with additional properties
data class ProductColor(
    val name: String = "",          // Tên màu sắc, ví dụ: "Đỏ", "Xanh dương", "Đen"
    val image: String = "",         // Đường dẫn đến hình ảnh của sản phẩm với màu này
    val productCode: String = "",   // Mã sản phẩm theo màu, thường dùng để quản lý kho
    val stock: Int = 0,             // Số lượng tồn kho của sản phẩm với màu này
    val status: String = "available" // Trạng thái của màu: có sẵn, ẩn, hết hàng, sắp có
)