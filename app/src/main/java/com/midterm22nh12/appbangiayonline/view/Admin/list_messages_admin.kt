package com.midterm22nh12.appbangiayonline.view.Admin

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.midterm22nh12.appbangiayonline.Adapter.Message.ConversationAdapter
import com.midterm22nh12.appbangiayonline.Utils.ChatUtils
import com.midterm22nh12.appbangiayonline.databinding.ListMessageAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation

class list_messages_admin(
    private val context: Context,
    private val binding: ListMessageAdminBinding,
    private val lifecycleOwner: LifecycleOwner
) {
    private val TAG = "ListMessagesAdmin"
    private lateinit var conversationAdapter: ConversationAdapter
    private var chatViewModel = (context as MainActivityAdmin).chatViewModel
    private var currentTab = "all" // "all", "assigned", "pending"

    // Thêm biến để lưu ID cuộc hội thoại đã xem
    private var lastViewedConversationId: String? = null

    private var conversationsListener: ListenerRegistration? = null

    init {
        setupView()
        setupRecyclerView()
        setupObservers()
        // Hiển thị trạng thái loading
        binding.pbLoadListMessageAdmin.visibility = View.VISIBLE

        // Kiểm tra người dùng đã đăng nhập
        if (checkUserAuthenticated()) {
            // Tải danh sách cuộc hội thoại
            loadConversations()
        }
        setupConversationsRealTimeListener()
    }

    private fun setupView() {
        binding.ivBackListMessageAdmin.setOnClickListener {
            // Ẩn giao diện danh sách tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityAdmin).returnToPreviousOverlay()
        }
    }

    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            onConversationRead = { conversationId ->
                // Lưu ID cuộc hội thoại đã xem
                lastViewedConversationId = conversationId
            }
        ) { conversation ->
            // Lưu ID cuộc hội thoại đã xem
            lastViewedConversationId = conversation.id

            chatViewModel.selectConversation(conversation)

            // Hiển thị giao diện chat
            (context as MainActivityAdmin).showMessageAdmin(conversation)
        }

        binding.rvListMessageAdmin.apply {
            adapter = conversationAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Theo dõi danh sách cuộc hội thoại
        chatViewModel.conversations.observe(lifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                binding.pbLoadListMessageAdmin.visibility = View.GONE

                if (result.isSuccess) {
                    val allConversations = result.getOrNull() ?: emptyList()
                    Log.d(TAG, "Đã tải ${allConversations.size} cuộc hội thoại")

                    // Lọc danh sách theo tab đang chọn
                    val filteredConversations = when (currentTab) {
                        "all" -> allConversations
                        "assigned" -> allConversations.filter {
                            it.adminId == FirebaseAuth.getInstance().currentUser?.uid
                        }
                        "pending" -> allConversations.filter {
                            it.adminId.isEmpty() && it.status == "open"
                        }
                        else -> allConversations
                    }

                    // Sắp xếp cuộc hội thoại theo thời gian tin nhắn mới nhất (giảm dần)
                    val sortedConversations = filteredConversations.sortedByDescending { it.lastMessageTime }

                    // Cập nhật RecyclerView
                    conversationAdapter.updateConversations(sortedConversations)

                } else {
                    val error = result.exceptionOrNull()?.message ?: "Không thể tải danh sách cuộc hội thoại"
                    Log.e(TAG, "Lỗi tải danh sách cuộc hội thoại: $error")
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Theo dõi trạng thái loading
        chatViewModel.isLoading.observe(lifecycleOwner) { isLoading ->
            binding.pbLoadListMessageAdmin.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun checkUserAuthenticated(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e(TAG, "Không có người dùng đăng nhập")
            Toast.makeText(
                context,
                "Vui lòng đăng nhập để sử dụng tính năng chat",
                Toast.LENGTH_LONG
            ).show()
            binding.pbLoadListMessageAdmin.visibility = View.GONE
            return false
        }

        Log.d(TAG, "Người dùng đã đăng nhập: ${currentUser.uid}")
        return true
    }

     fun loadConversations() {
        binding.pbLoadListMessageAdmin.visibility = View.VISIBLE

        // Tải tất cả cuộc hội thoại mặc định
        chatViewModel.getAllConversations()
    }
    // Khi làm mới danh sách
    fun refreshConversations() {
        Log.d(TAG, "Làm mới danh sách cuộc hội thoại, lastViewedConversationId: $lastViewedConversationId")

        // Truyền ID cuộc hội thoại đã xem cho adapter
        conversationAdapter.setLastViewedConversationId(lastViewedConversationId)

        // Tải lại danh sách
        loadConversations()
    }
    fun setLastViewedConversationId(conversationId: String?) {
        lastViewedConversationId = conversationId
        // Cập nhật cho adapter nếu đã được khởi tạo
        if (::conversationAdapter.isInitialized) {
            conversationAdapter.setLastViewedConversationId(conversationId)
        }
    }
    // phương thức lắng nghe tin nhắn gửi đến cho admin
    private fun setupConversationsRealTimeListener() {
        // Đăng ký quan sát conversationUpdates từ ViewModel
        chatViewModel.conversationUpdates.observe(lifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                if (result.isSuccess) {
                    val conversation = result.getOrNull()
                    if (conversation != null) {
                        Log.d(TAG, "Cuộc hội thoại cập nhật trong list_message: ${conversation.id}, " +
                                "unreadByAdmin: ${conversation.unreadByAdmin}, " +
                                "unreadByUser: ${conversation.unreadByUser}")

                        // Cập nhật adapter
                        conversationAdapter.updateConversation(conversation)

                        // Nếu có tin nhắn mới và số tin nhắn chưa đọc > 0, đưa cuộc hội thoại lên đầu
                        val unreadCount = if (ChatUtils.isCurrentUserAdmin()) {
                            conversation.unreadByAdmin
                        } else {
                            conversation.unreadByUser
                        }

                        Log.d(TAG, "unreadCount: $unreadCount")

                        if (unreadCount > 0) {
                            Log.d(TAG, "Tải lại danh sách vì unreadCount > 0")
                            // Tải lại toàn bộ danh sách để sắp xếp lại
                            loadConversations()
                        }
                    }
                }
            }
        }

        Log.d(TAG, "Bắt đầu lắng nghe thay đổi cuộc hội thoại")
        // Bắt đầu lắng nghe từ ViewModel
        chatViewModel.startListeningToConversations()
    }

    // Cập nhật onDestroy
    fun onDestroy() {
        // Dừng lắng nghe từ ViewModel
        chatViewModel.stopAllListeners()
    }
}