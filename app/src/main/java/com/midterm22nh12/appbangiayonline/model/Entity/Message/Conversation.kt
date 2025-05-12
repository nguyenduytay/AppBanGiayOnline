package com.midterm22nh12.appbangiayonline.model.Entity.Message

import java.util.UUID

/**
 * Đại diện cho một cuộc hội thoại giữa khách hàng và admin
 */
data class Conversation(
    var id: String = UUID.randomUUID().toString(),
    val userId: String = "",  // ID của khách hàng
    val userFullName: String = "",  // Tên của khách hàng để hiển thị
    val adminId: String = "system",  // ID của admin đang xử lý
    val title: String = "",  // Tiêu đề cuộc hội thoại (ví dụ: "Hỗ trợ đơn hàng #123")
    val status: String = "open",  // Trạng thái: open, resolved, closed
    val lastMessage: String = "",  // Tin nhắn cuối cùng để hiển thị trong danh sách
    val lastMessageTime: Long = System.currentTimeMillis(),  // Thời gian tin nhắn cuối
    val unreadByUser: Int = 0,  // Số tin nhắn chưa đọc của khách hàng
    val unreadByAdmin: Int = 0,  // Số tin nhắn chưa đọc của admin
    val createdAt: Long = System.currentTimeMillis()  // Thời gian tạo cuộc hội thoại
)