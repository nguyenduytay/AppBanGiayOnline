package com.midterm22nh12.appbangiayonline.Utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Lớp tiện ích cho chức năng chat
 */
object ChatUtils {
    // Danh sách các ID người dùng có role admin
    private val adminIds = mutableListOf<String>()

    // Flag để kiểm tra xem đã tải danh sách admin chưa
    private var isAdminListLoaded = false

    /**
     * Kiểm tra xem người dùng hiện tại có phải là admin không
     */
    fun isCurrentUserAdmin(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return false

        // Option 1: Kiểm tra dựa trên danh sách admin đã lưu
        if (isAdminListLoaded) {
            return adminIds.contains(currentUser.uid)
        }

        // Option 2: Nếu chưa có thông tin, kiểm tra dựa vào email
        // Giả sử tất cả admin có email kết thúc bằng "@admin.com" hoặc có domain cụ thể
        val email = currentUser.email ?: return false
        return email.endsWith("@admin.com") || email.contains("admin")

        // Option 3: Nếu bạn lưu role trong Firestore, bạn nên tải nó ở phương thức khác
        // và sử dụng cache để tránh truy vấn liên tục, ví dụ:
        // return userRole == "admin" || userRole == "super_admin"
    }

    /**
     * Tải danh sách admin từ Firestore
     * Phương thức này nên được gọi khi người dùng đăng nhập
     */
    fun loadAdminList(callback: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance().collection("admins")
            .get()
            .addOnSuccessListener { documents ->
                adminIds.clear()
                for (document in documents) {
                    val id = document.id
                    adminIds.add(id)
                }
                isAdminListLoaded = true
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    /**
     * Kiểm tra xem một ID cụ thể có phải là admin không
     */
    fun isAdmin(userId: String): Boolean {
        // Option 1: Kiểm tra dựa trên danh sách đã lưu
        if (isAdminListLoaded) {
            return adminIds.contains(userId)
        }

        // Trả về mặc định nếu chưa tải được danh sách
        return false
    }

    /**
     * Clear cache khi người dùng đăng xuất
     */
    fun clearCache() {
        adminIds.clear()
        isAdminListLoaded = false
    }
}