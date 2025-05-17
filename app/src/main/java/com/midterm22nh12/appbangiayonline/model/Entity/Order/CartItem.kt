    package com.midterm22nh12.appbangiayonline.model.Entity.Order

    import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
    import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor

    /**
     * Lớp đại diện cho một sản phẩm trong giỏ hàng
     */
    data class CartItem(
        val product: Product,
        val selectedColor: ProductColor,
        val selectedSize: String,
        val quantity: Int = 1
    )