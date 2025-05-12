package com.midterm22nh12.appbangiayonline.model.Entity.Message

/**
 * Class tiện ích để kết hợp cuộc hội thoại với tin nhắn cuối cùng
 */
data class ConversationWithLastMessage(
    val conversation: Conversation,
    val lastMessage: Message? = null
)