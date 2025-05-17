package com.midterm22nh12.appbangiayonline.view.Admin

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.midterm22nh12.appbangiayonline.Adapter.Message.MessageAdapter
import com.midterm22nh12.appbangiayonline.databinding.ShopMessagesUserBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation

class messages_admin(
    private val context: Context,
    private val binding: ShopMessagesUserBinding,
    private val conversation: Conversation? = null,
    private val lifecycleOwner: LifecycleOwner
) {
    private lateinit var messageAdapter: MessageAdapter
    private var chatViewModel = (context as MainActivityAdmin).chatViewModel
    private val TAG = "MessagesAdmin"

    init {
        Log.d(TAG, "Initializing messages_admin")

        // Khởi tạo components
        setUpView()
        setupRecyclerView()
        setupObservers()

        // Hiển thị trạng thái loading
        binding.pbLoadShopMessagesUser.visibility = View.VISIBLE

        // Kiểm tra người dùng và trạng thái đăng nhập
        if (checkUserAuthenticated()) {
            // Nếu có conversation được truyền vào, chọn nó
            if (conversation != null) {
                chatViewModel.selectConversation(conversation)
            } else {
                Log.e(TAG, "Không có cuộc hội thoại được truyền vào")
                Toast.makeText(context, "Không thể tải cuộc hội thoại", Toast.LENGTH_SHORT).show()
                (context as MainActivityAdmin).returnToPreviousOverlay()
            }
        }
        if (chatViewModel.selectedConversation.value != null) {
            markConversationAsRead()
        }

        setupListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setUpView() {
        // Ẩn phần hiển thị sản phẩm vì admin không cần
        binding.llSuggestShopMessagesUser.visibility = View.GONE
        binding.llTipShopMessagesUser.visibility = View.GONE

        // Hiển thị thông tin về cuộc hội thoại
        if (conversation != null) {
            binding.tvNameShopMessagesUser.text = "Chat với: ${conversation.userFullName}"
        }
        binding.ivBackShopMessagesUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang danh sách cuộc hội thoại
            (context as MainActivityAdmin).returnToPreviousOverlay()
            // Ẩn bàn phím
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etEditShopMessagesUser.windowToken, 0)
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
            binding.pbLoadShopMessagesUser.visibility = View.GONE
            return false
        }

        Log.d(TAG, "Người dùng đã đăng nhập: ${currentUser.uid}")
        return true
    }

    private fun setupRecyclerView() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d(TAG, "Thiết lập RecyclerView với userId: $currentUserId")

        messageAdapter = MessageAdapter(
            currentUserId = currentUserId,
            isAdminView = true
        )

        binding.rcMessageShopMessagesUser.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true  // Hiển thị tin nhắn mới nhất ở cuối
            }
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Theo dõi tin nhắn
        chatViewModel.messages.observe(lifecycleOwner) { event ->
            // Log để debug
            Log.d(TAG, "Nhận được event tin nhắn")

            // Lấy dữ liệu từ event
            val result = event.peekContent()

            if (result.isSuccess) {
                val messages = result.getOrNull() ?: emptyList()
                Log.d(TAG, "Tin nhắn đã nhận: ${messages.size}")

                // Cập nhật adapter
                messageAdapter.updateMessages(messages)

                // Cuộn xuống tin nhắn cuối cùng nếu có tin nhắn
                if (messages.isNotEmpty()) {
                    binding.rcMessageShopMessagesUser.post {
                        try {
                            binding.rcMessageShopMessagesUser.scrollToPosition(messages.size - 1)
                        } catch (e: Exception) {
                            Log.e(TAG, "Lỗi khi cuộn xuống tin nhắn cuối: ${e.message}")
                        }
                    }
                }

                // Ẩn loading
                binding.pbLoadShopMessagesUser.visibility = View.GONE
            } else {
                val error = result.exceptionOrNull()?.message ?: "Đã xảy ra lỗi khi tải tin nhắn"
                Log.e(TAG, "Lỗi tải tin nhắn: $error")
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()

                // Ẩn loading
                binding.pbLoadShopMessagesUser.visibility = View.GONE
            }

            // Đánh dấu event là đã xử lý
            event.getContentIfNotHandled()
        }

        // Theo dõi kết quả gửi tin nhắn
        chatViewModel.sendMessageResult.observe(lifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when {
                    result.isSuccess -> {
                        binding.etEditShopMessagesUser.setText("")
                        Log.d(TAG, "Đã gửi tin nhắn thành công")

                        // Đảm bảo tin nhắn mới nhất được hiển thị
                        val message = result.getOrNull()
                        message?.let {
                            // Thêm tin nhắn mới vào adapter nếu cần
                            messageAdapter.addMessage(it)

                            // Cuộn xuống tin nhắn mới nhất
                            binding.rcMessageShopMessagesUser.post {
                                try {
                                    val position = messageAdapter.itemCount - 1
                                    if (position >= 0) {
                                        binding.rcMessageShopMessagesUser.scrollToPosition(position)
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Lỗi khi cuộn xuống tin nhắn mới: ${e.message}")
                                }
                            }
                        }
                    }

                    result.isFailure -> {
                        val exception = result.exceptionOrNull()
                        Log.e(TAG, "Lỗi gửi tin nhắn", exception)
                        val errorMsg = when {
                            exception?.message?.contains("permission") == true -> "Không có quyền gửi tin nhắn"
                            exception?.message?.contains("network") == true -> "Lỗi kết nối mạng"
                            exception?.message?.contains("conversation") == true -> "Cuộc hội thoại không tồn tại"
                            else -> "Không thể gửi tin nhắn: ${exception?.message}"
                        }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Theo dõi kết quả cập nhật trạng thái
        chatViewModel.updateStatusResult.observe(lifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when {
                    result.isSuccess -> {
                        Toast.makeText(
                            context,
                            "Đã cập nhật trạng thái cuộc hội thoại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    result.isFailure -> {
                        val error =
                            result.exceptionOrNull()?.message ?: "Không thể cập nhật trạng thái"
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Hiển thị trạng thái loading
        chatViewModel.isLoading.observe(lifecycleOwner) { isLoading ->
            binding.pbLoadShopMessagesUser.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Theo dõi cuộc hội thoại đã chọn
        chatViewModel.selectedConversation.observe(lifecycleOwner) { conversation ->
            if (conversation != null) {
                Log.d(TAG, "Đã chọn cuộc hội thoại: ${conversation.id}")

                // Bắt đầu lắng nghe tin nhắn theo thời gian thực
                chatViewModel.startListeningToMessages(conversation.id)

                // Đánh dấu tin nhắn là đã đọc
                chatViewModel.markMessagesAsRead(conversation.id)
            } else {
                Log.d(TAG, "Không có cuộc hội thoại nào được chọn")
            }
        }
    }

    private fun setupListeners() {
        binding.ivSendShopMessagesUser.setOnClickListener {
            val messageContent = binding.etEditShopMessagesUser.text.toString().trim()
            if (messageContent.isEmpty()) {
                return@setOnClickListener
            }

            val conversation = chatViewModel.selectedConversation.value

            if (conversation == null) {
                Log.d(TAG, "Không có cuộc hội thoại được chọn khi cố gắng gửi tin nhắn")
                Toast.makeText(
                    context,
                    "Không thể gửi tin nhắn, cuộc hội thoại không tồn tại",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Đã có cuộc hội thoại, gửi tin nhắn ngay
                Log.d(TAG, "Gửi tin nhắn với cuộc hội thoại hiện có: ${conversation.id}")
                chatViewModel.sendMessage(
                    conversationId = conversation.id,
                    content = messageContent
                )
            }
        }
    }

    // Thêm phương thức đọc tin nhắn khi click vào
    private fun markConversationAsRead() {
        val conversation = chatViewModel.selectedConversation.value
        if (conversation != null) {
            Log.d(TAG, "Đánh dấu cuộc hội thoại đã đọc: ${conversation.id}")
            chatViewModel.markMessagesAsRead(conversation.id)
        }
    }
    fun getCurrentConversationId(): String? {
        return conversation?.id
    }
}