package com.midterm22nh12.appbangiayonline.model.Entity.Order

// Lớp để theo dõi lịch sử trạng thái đơn hàng
data class OrderStatusHistory(
    val orderId: String = "",         // ID đơn hàng
    val status: String = "",          // Trạng thái
    val timestamp: Long = System.currentTimeMillis(), // Thời gian cập nhật
    val note: String = "",            // Ghi chú cho trạng thái này
    val updatedBy: String = ""        // Người cập nhật (user ID hoặc system)
)