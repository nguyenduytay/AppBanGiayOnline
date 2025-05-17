package com.midterm22nh12.appbangiayonline.viewmodel.Message

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.midterm22nh12.appbangiayonline.Repository.ChatRepository
import com.midterm22nh12.appbangiayonline.Utils.Event
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation

/**
 * ViewModel dành riêng cho giao diện Admin Chat
 */
class AdminChatViewModel(application: Application) : AndroidViewModel(application) {
    private val chatRepository = ChatRepository()

    // LiveData cho danh sách cuộc hội thoại đang chờ xử lý (chưa có admin)
    private val _pendingConversations = MutableLiveData<Event<Result<List<Conversation>>>>()
    val pendingConversations: LiveData<Event<Result<List<Conversation>>>> = _pendingConversations

    // LiveData cho danh sách cuộc hội thoại đang xử lý bởi admin hiện tại
    private val _myAssignedConversations = MutableLiveData<Event<Result<List<Conversation>>>>()
    val myAssignedConversations: LiveData<Event<Result<List<Conversation>>>> = _myAssignedConversations

    // LiveData cho kết quả tiếp nhận cuộc hội thoại
    private val _takeConversationResult = MutableLiveData<Event<Result<Unit>>>()
    val takeConversationResult: LiveData<Event<Result<Unit>>> = _takeConversationResult

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Lấy danh sách cuộc hội thoại đang chờ xử lý (chưa có admin)
     */
    fun getPendingConversations() {
        _isLoading.value = true

        chatRepository.getAllConversations { result ->
            if (result.isSuccess) {
                val conversations = result.getOrNull() ?: emptyList()
                // Lọc ra các cuộc hội thoại chưa có admin và đang mở
                val pending = conversations.filter { it.adminId.isEmpty() && it.status == "open" }
                _pendingConversations.value = Event(Result.success(pending))
            } else {
                _pendingConversations.value = Event(result)
            }
            _isLoading.value = false
        }
    }

    /**
     * Lấy danh sách cuộc hội thoại đang được xử lý bởi admin hiện tại
     */
    fun getMyAssignedConversations() {
        _isLoading.value = true

        chatRepository.getAdminConversations { result ->
            _myAssignedConversations.value = Event(result)
            _isLoading.value = false
        }
    }

    /**
     * Tiếp nhận cuộc hội thoại (gán admin hiện tại làm người xử lý)
     */
    fun takeConversation(conversationId: String) {
        _isLoading.value = true

        // Lấy ID của admin hiện tại (người dùng đang đăng nhập)
        val currentAdminId = chatRepository.getCurrentUserId()

        if (currentAdminId.isEmpty()) {
            _takeConversationResult.value = Event(Result.failure(Exception("Người dùng chưa đăng nhập")))
            _isLoading.value = false
            return
        }

        chatRepository.assignAdminToConversation(conversationId, currentAdminId) { result ->
            _takeConversationResult.value = Event(result)
            _isLoading.value = false

            // Nếu thành công, cập nhật danh sách cuộc hội thoại
            if (result.isSuccess) {
                getPendingConversations()
                getMyAssignedConversations()
            }
        }
    }

    /**
     * Đánh dấu cuộc hội thoại là đã giải quyết
     */
    fun resolveConversation(conversationId: String) {
        _isLoading.value = true

        chatRepository.updateConversationStatus(conversationId, "resolved") { result ->
            _isLoading.value = false

            // Nếu thành công, cập nhật danh sách cuộc hội thoại
            if (result.isSuccess) {
                getMyAssignedConversations()
            }
        }
    }

    /**
     * Đánh dấu cuộc hội thoại là đã đóng
     */
    fun closeConversation(conversationId: String) {
        _isLoading.value = true

        chatRepository.updateConversationStatus(conversationId, "closed") { result ->
            _isLoading.value = false

            // Nếu thành công, cập nhật danh sách cuộc hội thoại
            if (result.isSuccess) {
                getMyAssignedConversations()
            }
        }
    }
}