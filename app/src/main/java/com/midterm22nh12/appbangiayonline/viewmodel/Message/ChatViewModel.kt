package com.midterm22nh12.appbangiayonline.viewmodel.Message

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.midterm22nh12.appbangiayonline.Repository.ChatRepository
import com.midterm22nh12.appbangiayonline.Utils.Event
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Message
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val chatRepository = ChatRepository()

    // LiveData cho danh sách cuộc hội thoại
    private val _conversations = MutableLiveData<Event<Result<List<Conversation>>>>()
    val conversations: LiveData<Event<Result<List<Conversation>>>> = _conversations

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

    // LiveData cho kết quả đánh dấu tin nhắn đã đọc
    private val _markAsReadResult = MutableLiveData<Event<Result<Unit>>>()
    val markAsReadResult: LiveData<Event<Result<Unit>>> = _markAsReadResult

    // LiveData cho kết quả cập nhật trạng thái cuộc hội thoại
    private val _updateStatusResult = MutableLiveData<Event<Result<Unit>>>()
    val updateStatusResult: LiveData<Event<Result<Unit>>> = _updateStatusResult

    // LiveData cho kết quả gán admin
    private val _assignAdminResult = MutableLiveData<Event<Result<Unit>>>()
    val assignAdminResult: LiveData<Event<Result<Unit>>> = _assignAdminResult

    // LiveData để thông báo thay đổi cuộc hội thoại
    private val _conversationUpdates = MutableLiveData<Event<Result<Conversation>>>()
    val conversationUpdates: LiveData<Event<Result<Conversation>>> = _conversationUpdates

    // LiveData để thông báo số lượng cuộc hội thoại chưa đọc
    private val _unreadConversationsCount = MutableLiveData<Int>(0)
    val unreadConversationsCount: LiveData<Int> = _unreadConversationsCount

    // Các listener để hủy khi không cần
    private var conversationsListener: ListenerRegistration? = null
    private var unreadCountListener: ListenerRegistration? = null

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
            _conversations.value = Event(result)
            _isLoading.value = false
        }
    }

    /**
     * Lấy danh sách cuộc hội thoại của admin
     */
    fun getAdminConversations() {
        _isLoading.value = true

        chatRepository.getAdminConversations { result ->
            _conversations.value = Event(result)
            _isLoading.value = false
        }
    }

    /**
     * Lấy danh sách tất cả cuộc hội thoại (dành cho super admin)
     */
    fun getAllConversations() {
        _isLoading.value = true

        chatRepository.getAllConversations { result ->
            _conversations.value = Event(result)
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
        _isLoading.value = true

        chatRepository.markMessagesAsReadForUser(conversationId) { result ->
            _markAsReadResult.value = Event(result)
            _isLoading.value = false

            // Cập nhật UI khi đánh dấu đã đọc thành công
            if (result.isSuccess) {
                Log.d("ChatViewModel", "Đánh dấu đã đọc thành công, cập nhật UI ${conversationId}")
                // Tải lại tin nhắn để cập nhật UI
                getConversationMessages(conversationId)
            } else {
                Log.e("ChatViewModel", "Lỗi đánh dấu đã đọc: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Cập nhật trạng thái cuộc hội thoại
     */
    fun updateConversationStatus(conversationId: String, newStatus: String) {
        _isLoading.value = true

        chatRepository.updateConversationStatus(conversationId, newStatus) { result ->
            _updateStatusResult.value = Event(result)
            _isLoading.value = false
        }
    }

    /**
     * Gán admin cho cuộc hội thoại
     */
    fun assignAdminToConversation(conversationId: String, adminId: String) {
        _isLoading.value = true

        chatRepository.assignAdminToConversation(conversationId, adminId) { result ->
            _assignAdminResult.value = Event(result)
            _isLoading.value = false
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
    /**
     * Bắt đầu lắng nghe số lượng cuộc hội thoại chưa đọc
     */
    fun startListeningToUnreadCount() {
        // Hủy listener cũ nếu có
        unreadCountListener?.remove()

        // Thiết lập listener mới
        unreadCountListener = chatRepository.setupUnreadConversationsCounter { count ->
            Log.d("ChatViewModel", "Số lượng cuộc hội thoại chưa đọc: $count")
            _unreadConversationsCount.postValue(count)
        }
    }

    /**
     * Dừng lắng nghe số lượng cuộc hội thoại chưa đọc
     */
    fun stopListeningToUnreadCount() {
        unreadCountListener?.remove()
        unreadCountListener = null
    }

    /**
     * Bắt đầu lắng nghe thay đổi của cuộc hội thoại
     */
    fun startListeningToConversations() {
        // Hủy listener cũ nếu có
        conversationsListener?.remove()

        // Thiết lập listener mới
        conversationsListener = chatRepository.setupConversationsRealTimeListener { conversation ->
            Log.d("ChatViewModel", "Nhận cập nhật cuộc hội thoại: ${conversation.id}, unreadByAdmin: ${conversation.unreadByAdmin}")

            // Gửi thông báo thông qua LiveData
            _conversationUpdates.postValue(Event(Result.success(conversation)))
        }
    }

    /**
     * Dừng lắng nghe thay đổi cuộc hội thoại
     */
    fun stopListeningToConversations() {
        conversationsListener?.remove()
        conversationsListener = null
    }

    /**
     * Dừng tất cả các listener
     */
    fun stopAllListeners() {
        stopListeningToConversations()
        stopListeningToUnreadCount()
    }

    // Đảm bảo hủy listener khi ViewModel bị hủy
    override fun onCleared() {
        super.onCleared()
        stopAllListeners()
    }
}