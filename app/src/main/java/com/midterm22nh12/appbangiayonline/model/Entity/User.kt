package com.midterm22nh12.appbangiayonline.model.Entity

/**
 * Lớp đại diện cho thông tin người dùng trong ứng dụng
 * Được sử dụng để lưu trữ và quản lý dữ liệu người dùng
 */
data class User(
    /** ID duy nhất của người dùng, thường là Firebase UID */
    val uid: String = "",

    /** Họ và tên đầy đủ của người dùng */
    val fullName: String = "",

    /** Tên đăng nhập duy nhất của người dùng */
    val username: String = "",

    /** Địa chỉ email của người dùng */
    val email: String = "",

    /** Số điện thoại liên hệ của người dùng */
    val phone: String = "",

    /** Địa chỉ giao hàng của người dùng */
    val address: String = "",  // Default empty address field

    /** Xác định người dùng có quyền admin hay không */
    val isAdmin: Boolean = false,

    /** Thời điểm tạo tài khoản (timestamp) */
    val createdAt: Long = 0
)