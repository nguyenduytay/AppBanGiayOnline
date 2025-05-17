package com.midterm22nh12.appbangiayonline.model.Entity.Order

/**
 * Lớp kết hợp Order và danh sách OrderItem
 */
data class OrderWithItems(
    val order: Order,               // Thông tin đơn hàng (mã đơn hàng, ngày đặt, trạng thái, v.v.)
    val items: List<OrderItem>      // Danh sách các mục hàng trong đơn hàng
)