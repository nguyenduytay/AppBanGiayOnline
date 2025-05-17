package com.midterm22nh12.appbangiayonline.model.Entity.Order

// Lớp đánh giá sản phẩm sau khi mua hàng
data class ProductReview(
    val id: String = "",              // ID đánh giá
    val orderId: String = "",         // ID đơn hàng
    val orderItemId: String = "",     // ID sản phẩm trong đơn hàng
    val userId: String = "",          // ID người dùng đánh giá
    val productId: String = "",       // ID sản phẩm được đánh giá
    val productName: String = "",     // Tên sản phẩm
    val colorName: String = "",       // Tên màu sắc
    val size: String = "",            // Size sản phẩm
    val rating: Float = 0f,           // Số sao đánh giá (1-5)
    val comment: String = "",         // Nội dung đánh giá
    val images: List<String> = emptyList(), // Ảnh đính kèm đánh giá
    val createdAt: Long = System.currentTimeMillis(), // Thời gian đánh giá
)