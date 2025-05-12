package com.midterm22nh12.appbangiayonline.model.Entity.Order

// Thêm trường orderId vào OrderItem để dễ truy vấn
data class OrderItem(
    val id: String = "",           // ID của order item
    val orderId: String = "",      // ID của đơn hàng
    val productId: String = "",    // ID của sản phẩm
    val productName: String = "",  // Tên sản phẩm
    val productImage: String = "", // Ảnh sản phẩm
    val price: Long = 0,           // Giá sản phẩm
    val quantity: Int = 1,         // Số lượng
    val size: String = "",         // Kích cỡ đã chọn
    val color: String = "",        // Màu sắc đã chọn
    val colorImage: String = "",   // Ảnh của màu sắc đã chọn
    val productCode: String = ""   // Mã sản phẩm của màu đã chọn
)