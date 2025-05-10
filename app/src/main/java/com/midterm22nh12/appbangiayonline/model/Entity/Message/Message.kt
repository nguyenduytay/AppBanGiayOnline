package com.midterm22nh12.appbangiayonline.model.Entity.Message

import java.util.UUID

/**
 * Đại diện cho một tin nhắn trong cuộc hội thoại
 */
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",  // ID của cuộc hội thoại
    val senderId: String = "",  // ID của người gửi (có thể là user hoặc admin)
    val senderName: String = "",  // Tên người gửi để hiển thị
    val content: String = "",  // Nội dung tin nhắn
    val timestamp: Long = System.currentTimeMillis(),  // Thời gian gửi
    val isRead: Boolean = false,  // Đã đọc chưa
    val type: String = "text",  // Loại tin nhắn: text, image, attachment
    val attachmentUrl: String = "",  // URL của file đính kèm (nếu có)
    val isSystemMessage: Boolean = false  // Tin nhắn hệ thống (ví dụ: "Admin đã tham gia cuộc hội thoại")
)