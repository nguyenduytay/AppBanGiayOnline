package com.midterm22nh12.appbangiayonline.model.Entity.Product

data class Brand(
    val id: String = "",            // Mã định danh duy nhất của thương hiệu
    val name: String = "",          // Tên thương hiệu (Nike, Adidas, Puma, v.v.)
    val image: String = ""          // URL hình ảnh logo của thương hiệu
)