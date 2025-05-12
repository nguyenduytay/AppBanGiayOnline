package com.midterm22nh12.appbangiayonline.model.Entity.Message

import java.util.UUID

/**
 * Đại diện cho một mục đính kèm (ảnh, tệp) trong cuộc hội thoại
 */
data class ChatAttachment(
    val id: String = UUID.randomUUID().toString(),
    val messageId: String = "",  // ID của tin nhắn chứa đính kèm này
    val url: String = "",  // URL của file đính kèm
    val type: String = "",  // Loại file: image, document, etc.
    val fileName: String = "",  // Tên file
    val fileSize: Long = 0,  // Kích thước file (bytes)
    val uploadedAt: Long = System.currentTimeMillis()  // Thời gian tải lên
)