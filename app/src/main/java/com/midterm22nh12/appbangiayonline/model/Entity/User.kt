package com.midterm22nh12.appbangiayonline.model.Entity


data class User(
    val uid: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",  // Default empty address field
    val isAdmin: Boolean = false,
    val createdAt: Long = 0
)