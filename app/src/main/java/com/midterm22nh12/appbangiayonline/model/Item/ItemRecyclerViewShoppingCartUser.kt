package com.midterm22nh12.appbangiayonline.model.Item

data class ItemRecyclerViewShoppingCartUser(
    val id: String, // ID duy nhất cho item (thường là productId_color_size)
    val productId: String, // ID của sản phẩm
    val productName: String, // Tên sản phẩm
    val price: Long, // Giá sản phẩm
    var quantity: Int, // Số lượng đã chọn
    val colorName: String, // Tên màu sắc
    val size: String, // Kích cỡ
    val imageUrl: String, // URL hình ảnh
    val stock: Int, // Số lượng tồn kho
    var isSelected: Boolean = false // Trạng thái chọn/không chọn
)