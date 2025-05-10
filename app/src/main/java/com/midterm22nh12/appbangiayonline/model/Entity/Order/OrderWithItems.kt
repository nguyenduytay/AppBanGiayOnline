package com.midterm22nh12.appbangiayonline.model.Entity.Order

/**
 * Lớp kết hợp Order và danh sách OrderItem
 */
data class OrderWithItems(
    val order: Order,
    val items: List<OrderItem>
)