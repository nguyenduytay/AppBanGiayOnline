package com.midterm22nh12.appbangiayonline.model.Entity

// Size class with status
data class ProductSize(
    val value: String = "",
    val status: String = "available" // available, hidden, out_of_stock, coming_soon
)