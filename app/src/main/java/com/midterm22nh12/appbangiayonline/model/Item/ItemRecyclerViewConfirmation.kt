package com.midterm22nh12.appbangiayonline.model.Item

data class ItemRecyclerViewConfirmation (
    val orderItemId: String?, // ID của item trong đơn hàng
    val orderId: String?, // ID đơn hàng
    val productId: String?, // ID sản phẩm
    val productName: String?, // Tên sản phẩm
    val price: Long?, // Giá sản phẩm
    val quantity: Int?, // Số lượng
    val colorName: String?, // Tên màu sắc
    val size: String?, // Kích cỡ
    val productImage: String?, // URL hình ảnh sản phẩm
    val orderDate: Long?, // Ngày đặt hàng
    val status: String = "pending" // Trạng thái mặc định
)