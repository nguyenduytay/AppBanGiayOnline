package com.midterm22nh12.appbangiayonline.model.Entity

// Color class with additional properties
data class ProductColor(
    val name: String = "",
    val image: String = "",
    val productCode: String = "",
    val stock: Int = 0,
    val status: String = "available" // available, hidden, out_of_stock, coming_soon
)