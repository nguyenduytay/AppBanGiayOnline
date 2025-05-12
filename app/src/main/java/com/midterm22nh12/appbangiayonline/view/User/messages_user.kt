package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midterm22nh12.appbangiayonline.Adapter.Message.MessageAdapter
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.Utils.Event
import com.midterm22nh12.appbangiayonline.databinding.ShopMessagesUserBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class messages_user(
    private val context: Context,
    private val binding: ShopMessagesUserBinding,
    private val item: ItemRecyclerViewProductHomeUser? = null,
    private val lifecycleOwner: LifecycleOwner
) {
    private lateinit var messageAdapter: MessageAdapter
    private var chatViewModel = (context as MainActivityUser).getSharedChatViewModel()
    private val TAG = "ChatDebug"

    init {
        Log.d(TAG, "Initializing messages_user")

        // Khởi tạo components
        setUpView()
        showProductMessage()
        setupRecyclerView()

        // Thiết lập các observers
        setupObservers()

        // Hiển thị trạng thái loading
        binding.pbLoadShopMessagesUser.visibility = View.VISIBLE

        // Kiểm tra người dùng và trạng thái đăng nhập
        checkUserAuthenticated()

        // Tìm và tải cuộc hội thoại hiện có hoặc tạo mới
        findOrCreateConversation()

        setupListeners()

        if (chatViewModel.selectedConversation.value != null) {
            markConversationAsRead()
        }
    }

    private fun setUpView() {
        binding.ivBackShopMessagesUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
            // Ẩn bàn phím
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etEditShopMessagesUser.windowToken, 0)
        }
    }

    //hiển thị sản phẩm
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun showProductMessage() {
        if (item == null) {
            binding.llSuggestShopMessagesUser.visibility = View.GONE
            binding.llTipShopMessagesUser.visibility = View.GONE
        } else {
            binding.llSuggestShopMessagesUser.visibility = View.VISIBLE
            binding.llTipShopMessagesUser.visibility = View.VISIBLE
            binding.tvNameProductShopMessageUser.text = item.name
            binding.tvPriceProductShopMessagesUser.text = String.format("%,d vnđ", item.price)
            binding.tvTip1ShopMessagesUser.text = "Sản phẩm ${item.name} còn không ?"
            binding.tvTip2ShopMessagesUser.text = "Sản phẩm ${item.name} còn màu khác không ?"
            binding.tvTip3ShopMessagesUser.text = "Sản phẩm ${item.name} còn size khác không ?"
            Glide.with(binding.ivImageProductShopMessagesUser.context)
                .load(item.colors[0].image)
                .placeholder(R.drawable.shoes1)
                .error(R.drawable.shoes1)
                .into(binding.ivImageProductShopMessagesUser)

            binding.btAskShopMessagesUser.setOnClickListener {
                binding.llTipShopMessagesUser.visibility = View.GONE
                val conversation = chatViewModel.selectedConversation.value
                if (conversation != null) {
                    chatViewModel.sendMessage(
                        conversationId = conversation.id,
                        content = "Xin chào, tôi cần hỗ trợ về sản phẩm : ${item.name}"
                    )
                } else {
                    // Nếu chưa có cuộc hội thoại, tạo mới và gửi sau
                    val pendingMessage = "Xin chào, tôi cần hỗ trợ về sản phẩm : ${item.name}"
                    // Lưu tin nhắn tạm thời
                    val observer = object : Observer<Event<Result<Conversation>>> {
                        override fun onChanged(event: Event<Result<Conversation>>) {
                            event.getContentIfNotHandled()?.let { result ->
                                if (result.isSuccess) {
                                    chatViewModel.createConversationResult.removeObserver(this)
                                    val newConversation = result.getOrNull()
                                    if (newConversation != null) {
                                        chatViewModel.sendMessage(
                                            conversationId = newConversation.id,
                                            content = pendingMessage
                                        )
                                    }
                                }
                            }
                        }
                    }
                    chatViewModel.createConversationResult.observe(lifecycleOwner, observer)
                    initializeConversation()
                }
            }

            // Cập nhật các gợi ý để sử dụng cuộc hội thoại hiện tại
            binding.tvTip1ShopMessagesUser.setOnClickListener {
                val conversation = chatViewModel.selectedConversation.value
                if (conversation != null) {
                    chatViewModel.sendMessage(
                        conversationId = conversation.id,
                        content = binding.tvTip1ShopMessagesUser.text.toString()
                    )
                    binding.llTipShopMessagesUser.visibility = View.GONE
                }
            }

            binding.tvTip2ShopMessagesUser.setOnClickListener {
                val conversation = chatViewModel.selectedConversation.value
                if (conversation != null) {
                    chatViewModel.sendMessage(
                        conversationId = conversation.id,
                        content = binding.tvTip2ShopMessagesUser.text.toString()
                    )
                    binding.llTipShopMessagesUser.visibility = View.GONE
                }
            }

            binding.tvTip3ShopMessagesUser.setOnClickListener {
                val conversation = chatViewModel.selectedConversation.value
                if (conversation != null) {
                    chatViewModel.sendMessage(
                        conversationId = conversation.id,
                        content = binding.tvTip3ShopMessagesUser.text.toString()
                    )
                    binding.llTipShopMessagesUser.visibility = View.GONE
                }
            }
        }
    }

    // Kiểm tra người dùng đã đăng nhập
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

    // Tìm cuộc hội thoại hiện có hoặc tạo mới
    private fun findOrCreateConversation() {
        // Nếu đã có cuộc hội thoại được chọn, sử dụng nó
        if (chatViewModel.selectedConversation.value != null) {
            val conversation = chatViewModel.selectedConversation.value
            Log.d(TAG, "Đã có cuộc hội thoại được chọn: ${conversation?.id}")
            conversation?.let {
                // Bắt đầu lắng nghe tin nhắn theo thời gian thực
                chatViewModel.startListeningToMessages(it.id)
                // Đánh dấu tin nhắn là đã đọc
                chatViewModel.markMessagesAsRead(it.id)
            }
            return
        }

        // Kiểm tra xem đã có ID cuộc hội thoại được lưu trong SharedPreferences chưa
        val savedConversationId = getSavedConversationId("default")
        if (savedConversationId != null) {
            // Nếu đã có ID, tải cuộc hội thoại từ ID này
            loadConversationById(savedConversationId)
            return
        }

        // Nếu chưa có, tìm trong Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        Log.d(TAG, "Tìm kiếm cuộc hội thoại cho người dùng: ${currentUser.uid}")

        // Tìm cuộc hội thoại hiện có dựa trên userId
        FirebaseFirestore.getInstance().collection("conversations")
            .whereEqualTo("userId", currentUser.uid)
            .limit(1)  // Chỉ lấy 1 cuộc hội thoại đầu tiên tìm thấy
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Tìm thấy cuộc hội thoại hiện có
                    val conversation = documents.documents[0].toObject(Conversation::class.java)
                    if (conversation != null) {
                        Log.d(TAG, "Đã tìm thấy cuộc hội thoại hiện có: ${conversation.id}")
                        // Chọn cuộc hội thoại này
                        chatViewModel.selectConversation(conversation)

                        // Lưu ID cuộc hội thoại để sử dụng sau này
                        saveConversationId(conversation.id, "default")
                    } else {
                        Log.e(TAG, "Lỗi chuyển đổi dữ liệu cuộc hội thoại")
                        // Tạo mới một cuộc hội thoại duy nhất
                        initializeConversation()
                    }
                } else {
                    // Không tìm thấy cuộc hội thoại, tạo mới
                    Log.d(TAG, "Không tìm thấy cuộc hội thoại hiện có, tạo mới")
                    initializeConversation()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi khi tìm kiếm cuộc hội thoại: ${e.message}")
                // Xảy ra lỗi, tạo cuộc hội thoại mới
                initializeConversation()
            }
    }

    // Tải cuộc hội thoại từ ID
    private fun loadConversationById(conversationId: String) {
        FirebaseFirestore.getInstance().collection("conversations")
            .document(conversationId)
            .get()
            .addOnSuccessListener { document ->
                val conversation = document.toObject(Conversation::class.java)
                if (conversation != null) {
                    Log.d(TAG, "Đã tải cuộc hội thoại từ ID: $conversationId")
                    chatViewModel.selectConversation(conversation)
                    // Bắt đầu lắng nghe tin nhắn
                    chatViewModel.startListeningToMessages(conversationId)
                    // Đánh dấu tin nhắn là đã đọc
                    chatViewModel.markMessagesAsRead(conversationId)
                } else {
                    Log.e(TAG, "Không tìm thấy cuộc hội thoại với ID: $conversationId")
                    // ID không còn hợp lệ, xóa và tạo mới
                    clearSavedConversationId()
                    initializeConversation()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi khi tải cuộc hội thoại: ${e.message}")
                // Không thể tải, xóa ID và tạo mới
                clearSavedConversationId()
                initializeConversation()
            }
    }

    // Lưu ID cuộc hội thoại
    private fun saveConversationId(conversationId: String, key: String = "default") {
        val sharedPrefs = context.getSharedPreferences("chat_preferences", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString(key, conversationId)
        editor.apply()
        Log.d(TAG, "Đã lưu ID cuộc hội thoại: $conversationId với key: $key")
    }

    // Lấy ID cuộc hội thoại đã lưu
    private fun getSavedConversationId(key: String = "default"): String? {
        val sharedPrefs = context.getSharedPreferences("chat_preferences", Context.MODE_PRIVATE)
        val conversationId = sharedPrefs.getString(key, null)
        Log.d(TAG, "Đã lấy ID cuộc hội thoại: $conversationId với key: $key")
        return conversationId
    }

    // Xóa ID cuộc hội thoại đã lưu
    private fun clearSavedConversationId() {
        val sharedPrefs = context.getSharedPreferences("chat_preferences", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.remove("default")
        editor.apply()
        Log.d(TAG, "Đã xóa ID cuộc hội thoại")
    }

    private fun setupRecyclerView() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d(TAG, "Thiết lập RecyclerView với userId: $currentUserId")

        messageAdapter = MessageAdapter(currentUserId = currentUserId, isAdminView = false)

        binding.rcMessageShopMessagesUser.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true  // Hiển thị tin nhắn mới nhất ở cuối
            }

            // Đảm bảo RecyclerView có kích thước cố định để tối ưu hóa hiệu suất
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Theo dõi kết quả tạo cuộc hội thoại
        chatViewModel.createConversationResult.observe(lifecycleOwner) { event ->
            val result = event.peekContent()  // Xem nội dung mà không đánh dấu là đã xử lý

            if (result.isSuccess) {
                val newConversation = result.getOrNull()
                if (newConversation != null) {
                    Log.d(TAG, "Nhận được kết quả tạo cuộc hội thoại: ${newConversation.id}")

                    // Lưu ID cuộc hội thoại mới
                    saveConversationId(newConversation.id, "default")

                    // Tự động chọn cuộc hội thoại mới tạo
                    chatViewModel.selectConversation(newConversation)

                    // Bắt đầu lắng nghe tin nhắn ngay lập tức
                    chatViewModel.startListeningToMessages(newConversation.id)
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Không thể tạo cuộc hội thoại"
                Log.e(TAG, "Lỗi tạo cuộc hội thoại: $error")
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }

            // Đánh dấu event là đã xử lý
            event.getContentIfNotHandled()
        }

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
            binding.llTipShopMessagesUser.visibility = View.GONE
            val messageContent = binding.etEditShopMessagesUser.text.toString().trim()
            if (messageContent.isEmpty()) {
                return@setOnClickListener
            }

            val conversation = chatViewModel.selectedConversation.value

            if (conversation == null) {
                Log.d(TAG, "Không có cuộc hội thoại được chọn khi cố gắng gửi tin nhắn")
                // Kiểm tra xem đang tạo cuộc hội thoại mới hay không
                if (chatViewModel.isLoading.value == true) {
                    Toast.makeText(
                        context,
                        "Đang tạo cuộc hội thoại, vui lòng đợi...",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                // Lưu tin nhắn tạm thời
                val pendingMessage = messageContent
                // Tạo observer một lần để theo dõi kết quả tạo conversation
                val conversationObserver = object : Observer<Event<Result<Conversation>>> {
                    override fun onChanged(value: Event<Result<Conversation>>) {
                        // Kiểm tra kết quả chỉ một lần
                        value.getContentIfNotHandled()?.let { result ->
                            if (result.isSuccess) {
                                // Gỡ bỏ observer này để tránh rò rỉ bộ nhớ
                                chatViewModel.createConversationResult.removeObserver(this)

                                val newConversation = result.getOrNull()
                                if (newConversation != null) {
                                    // Gửi tin nhắn đang chờ
                                    chatViewModel.sendMessage(
                                        conversationId = newConversation.id,
                                        content = pendingMessage
                                    )
                                    Log.d(
                                        TAG,
                                        "Đã gửi tin nhắn sau khi tạo cuộc hội thoại: $pendingMessage"
                                    )
                                }
                            }
                        }
                    }
                }

                // Đăng ký observer
                chatViewModel.createConversationResult.observe(lifecycleOwner, conversationObserver)
                // Bắt đầu tạo cuộc hội thoại mới
                initializeConversation()
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

    private fun initializeConversation() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để gửi tin nhắn", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo tiêu đề cuộc hội thoại duy nhất với thông tin người dùng
        val initialTitle = "Chat hỗ trợ: ${currentUser.displayName ?: "Người dùng"}"

        // Tạo tin nhắn ban đầu tương ứng nếu có item
        val initialMessage = if (item != null) {
            "Xin chào, tôi cần hỗ trợ về sản phẩm: ${item.name}"
        } else {
            "Xin chào, tôi cần được hỗ trợ."
        }

        Log.d(TAG, "Tạo cuộc hội thoại duy nhất: $initialTitle")
        chatViewModel.createConversation(
            title = initialTitle,
            initialMessage = initialMessage
        )
    }

    // Thêm phương thức đọc tin nhắn khi click vào
    private fun markConversationAsRead() {
        val conversation = chatViewModel.selectedConversation.value
        if (conversation != null) {
            Log.d(TAG, "Đánh dấu cuộc hội thoại đã đọc: ${conversation.id}")
            chatViewModel.markMessagesAsRead(conversation.id)
        }
    }
}