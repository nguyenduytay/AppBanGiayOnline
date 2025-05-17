package com.midterm22nh12.appbangiayonline.model.Entity.Order

// Lớp để theo dõi lịch sử trạng thái đơn hàng
data class OrderStatusHistory(
    val id: String = "",              // ID lịch sử trạng thái
    val orderItemId: String,
    val orderId: String = "",         // ID đơn hàng
    val status: String = "pending",          // Trạng thái đơn hàng (pending, shipping, delivered, evaluate)
    val timestamp: Long = System.currentTimeMillis(), // Thời gian cập nhật
    val note: String = "",            // Ghi chú cho trạng thái này
    val updatedBy: String = "" ,       // Người cập nhật (user ID hoặc system)
    val productId: String = "",
    val price: Long = 0,           // Giá sản phẩm
    val quantity: Int = 1,         // Số lượng
    val size: String = "",         // Kích cỡ đã chọn
    val color: String = "",        // Màu sắc đã chọn
    val colorImage: String = "",   // Ảnh của màu sắc đã chọn
    val productCode: String = ""   // Mã sản phẩm của màu đã chọn
)