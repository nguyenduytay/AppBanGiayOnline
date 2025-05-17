package com.midterm22nh12.appbangiayonline.model.Entity.Order

import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor

/**
 * Lớp đại diện cho một sản phẩm trong giỏ hàng
 */
data class CartItem(
    val product: Product,           // Thông tin sản phẩm (ID, tên, giá, mô tả, v.v.)
    val selectedColor: ProductColor, // Màu sắc được chọn cho sản phẩm
    val selectedSize: String,       // Kích thước được chọn cho sản phẩm
    val quantity: Int = 1           // Số lượng sản phẩm, mặc định là 1
)