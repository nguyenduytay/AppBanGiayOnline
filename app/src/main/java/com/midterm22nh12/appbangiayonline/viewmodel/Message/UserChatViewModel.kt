package com.midterm22nh12.appbangiayonline.viewmodel.Message

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Repository.ChatRepository
import com.midterm22nh12.appbangiayonline.Utils.Event
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Message
import kotlinx.coroutines.launch

/**
 * ViewModel dành riêng cho giao diện User Chat
 */
class UserChatViewModel(application: Application) : AndroidViewModel(application) {
    private val chatRepository = ChatRepository()

    // LiveData cho danh sách cuộc hội thoại của người dùng
    private val _userConversations = MutableLiveData<Event<Result<List<Conversation>>>>()
    val userConversations: LiveData<Event<Result<List<Conversation>>>> = _userConversations

    // LiveData cho cuộc hội thoại đã chọn
    private val _selectedConversation = MutableLiveData<Conversation>()
    val selectedConversation: LiveData<Conversation> = _selectedConversation

    // LiveData cho tin nhắn trong cuộc hội thoại
    private val _messages = MutableLiveData<Event<Result<List<Message>>>>()
    val messages: LiveData<Event<Result<List<Message>>>> = _messages

    // LiveData cho kết quả tạo cuộc hội thoại
    private val _createConversationResult = MutableLiveData<Event<Result<Conversation>>>()
    val createConversationResult: LiveData<Event<Result<Conversation>>> = _createConversationResult

    // LiveData cho kết quả gửi tin nhắn
    private val _sendMessageResult = MutableLiveData<Event<Result<Message>>>()
    val sendMessageResult: LiveData<Event<Result<Message>>> = _sendMessageResult

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Tạo cuộc hội thoại mới
     */
    fun createConversation(title: String, initialMessage: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = chatRepository.createConversation(title, initialMessage)
                _createConversationResult.value = Event(result)

                // Nếu tạo thành công, cập nhật danh sách cuộc hội thoại
                if (result.isSuccess) {
                    getUserConversations()
                }
            } catch (e: Exception) {
                _createConversationResult.value = Event(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Gửi tin nhắn mới
     */
    fun sendMessage(conversationId: String, content: String, type: String = "text", attachmentUri: Uri? = null) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = chatRepository.sendMessage(conversationId, content, type, attachmentUri)
                _sendMessageResult.value = Event(result)
            } catch (e: Exception) {
                _sendMessageResult.value = Event(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Lấy danh sách cuộc hội thoại của người dùng
     */
    fun getUserConversations() {
        _isLoading.value = true

        chatRepository.getUserConversations { result ->
            _userConversations.value = Event(result)
            _isLoading.value = false
        }
    }

    /**
     * Lấy tin nhắn trong cuộc hội thoại
     */
    fun getConversationMessages(conversationId: String) {
        _isLoading.value = true

        chatRepository.getConversationMessages(conversationId) { result ->
            _messages.value = Event(result)
            _isLoading.value = false
        }
    }

    /**
     * Bắt đầu lắng nghe tin nhắn theo thời gian thực
     */
    fun startListeningToMessages(conversationId: String) {
        chatRepository.listenToConversationMessages(conversationId) { result ->
            _messages.value = Event(result)
        }
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    fun markMessagesAsRead(conversationId: String) {
        chatRepository.markMessagesAsReadForUser(conversationId) { _ ->
            // Không cần xử lý kết quả
        }
    }

    /**
     * Chọn một cuộc hội thoại
     */
    fun selectConversation(conversation: Conversation) {
        _selectedConversation.value = conversation

        // Đánh dấu tin nhắn trong cuộc hội thoại này là đã đọc
        markMessagesAsRead(conversation.id)

        // Bắt đầu lắng nghe tin nhắn mới
        startListeningToMessages(conversation.id)
    }
}