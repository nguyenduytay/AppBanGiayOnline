    package com.midterm22nh12.appbangiayonline.model.Entity.Order

    // Lớp đại diện cho một đơn hàng
    data class Order(
        val id: String = "",              // ID đơn hàng
        val userId: String = "",          // ID của người dùng đặt hàng
        val items: List<OrderItem> = emptyList(),  // Danh sách các sản phẩm trong đơn hàng
        val totalAmount: Long = 0,        // Tổng tiền đơn hàng
        val shippingAddress: String = "", // Địa chỉ giao hàng
        val phoneNumber: String = "",     // Số điện thoại liên hệ
        val paymentMethod: String = "",   // Phương thức thanh toán (COD, Banking, etc)
        val status: String = "pending",   // Trạng thái đơn hàng (pending, confirmed, shipping, delivered, cancelled)
        val createdAt: Long = System.currentTimeMillis(), // Thời gian tạo đơn hàng
        val updatedAt: Long = System.currentTimeMillis(), // Thời gian cập nhật gần nhất
        val note: String = "",            // Ghi chú của khách hàng
        val shippingFee: Long = 0,        // Phí vận chuyển
        val discount: Long = 0,           // Giảm giá (nếu có)
        val couponCode: String = "",      // Mã giảm giá sử dụng (nếu có)
        val paymentStatus: String = "unpaid" // Trạng thái thanh toán (unpaid, paid)
    )