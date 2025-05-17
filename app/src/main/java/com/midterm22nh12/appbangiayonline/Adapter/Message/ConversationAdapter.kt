package com.midterm22nh12.appbangiayonline.Adapter.Message

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.Utils.ChatUtils
import com.midterm22nh12.appbangiayonline.databinding.ItemListMessageAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ConversationAdapter(
    private val conversations: MutableList<Conversation> = mutableListOf(),
    private val onConversationRead: (String) -> Unit = {}, // Callback khi cuộc hội thoại được đọc
    private val onClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    // Thêm biến để lưu ID cuộc hội thoại đã xem
    private var lastViewedConversationId: String? = null

    // Phương thức để cập nhật ID cuộc hội thoại đã xem
    @SuppressLint("NotifyDataSetChanged")
    fun setLastViewedConversationId(conversationId: String?) {
        lastViewedConversationId = conversationId
        notifyDataSetChanged() // Cập nhật lại toàn bộ danh sách
    }
    fun updateConversation(updatedConversation: Conversation) {
        // Tìm vị trí của cuộc hội thoại trong danh sách
        val index = conversations.indexOfFirst { it.id == updatedConversation.id }
        if (index >= 0) {
            // Cập nhật cuộc hội thoại tại vị trí tìm thấy
            conversations[index] = updatedConversation
            notifyItemChanged(index)
            Log.d("ConversationAdapter", "Đã cập nhật cuộc hội thoại ${updatedConversation.id}: unreadByAdmin=${updatedConversation.unreadByAdmin}, unreadByUser=${updatedConversation.unreadByUser}")
        }
    }
    inner class ViewHolder(private val binding: ItemListMessageAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(conversation: Conversation) {
            binding.apply {
                // Xử lý userFullName rỗng
                tvUserAvatarItemListMessageAdmin.text = if (conversation.userFullName.isNotEmpty()) {
                    conversation.userFullName.first().uppercaseChar().toString()
                } else {
                    val userId = conversation.userId
                    if (userId.isNotEmpty()) {
                        // Lấy ký tự đầu tiên của userId làm ký tự đại diện
                        userId.first().uppercaseChar().toString()
                    } else {
                        "?" // Ký tự mặc định nếu cả hai đều rỗng
                    }
                }
                tvUserNameItemListMessageAdmin.text = conversation.userFullName

                // Kiểm tra và xử lý lastMessage rỗng
                val message = conversation.lastMessage.ifEmpty { "Không có tin nhắn" }
                tvLastMessageItemListMessageAdmin.text = if (message.length > 40) {
                    message.substring(0, 40) + "..."
                } else {
                    message
                }

                tvMessageTimeItemListMessageAdmin.text = formatDateTime(conversation.lastMessageTime)

                // Hiển thị số tin nhắn chưa đọc (khác nhau cho người dùng và admin)
                val unreadCount = if (ChatUtils.isCurrentUserAdmin()) {
                    conversation.unreadByUser
                } else {
                    conversation.unreadByAdmin
                }
                tvUnreadCountItemListMessageAdmin.visibility = View.VISIBLE
                // Ẩn badge số tin nhắn chưa đọc nếu là cuộc hội thoại vừa xem
                if (unreadCount > 0) {
                    if(unreadCount>9)
                    {
                        tvUnreadCountItemListMessageAdmin.text = "9+"
                    }
                    else {
                        tvUnreadCountItemListMessageAdmin.text = unreadCount.toString()
                    }
                } else {
                    tvUnreadCountItemListMessageAdmin.visibility = View.GONE
                }

                // Bắt sự kiện click
                root.setOnClickListener {
                    // Thông báo cuộc hội thoại đã được đọc
                    onConversationRead(conversation.id)

                    // Gọi callback onClick
                    onClick(conversation)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListMessageAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount() = conversations.size

    // Cập nhật danh sách cuộc hội thoại
    @SuppressLint("NotifyDataSetChanged")
    fun updateConversations(newConversations: List<Conversation>) {
        try {
            val diffCallback = ConversationDiffCallback(conversations, newConversations)
            val diffResult = DiffUtil.calculateDiff(diffCallback)

            conversations.clear()
            conversations.addAll(newConversations)
            diffResult.dispatchUpdatesTo(this)

            Log.d("ConversationAdapter", "Cập nhật ${newConversations.size} cuộc hội thoại thành công")
        } catch (e: Exception) {
            Log.e("ConversationAdapter", "Lỗi khi cập nhật: ${e.message}")
            e.printStackTrace()

            // Fallback: Cập nhật thủ công nếu DiffUtil thất bại
            conversations.clear()
            conversations.addAll(newConversations)
            notifyDataSetChanged()

            Log.d("ConversationAdapter", "Đã cập nhật thủ công ${newConversations.size} cuộc hội thoại")
        }
    }

    // Thêm vào đầu lớp để cache các đối tượng SimpleDateFormat
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    // Thêm hàm định dạng thời gian
    private fun formatDateTime(timestamp: Long): String {
        val messageDate = Date(timestamp)
        val currentDate = Date()

        return if (isSameDay(messageDate, currentDate)) {
            "Hôm nay " + timeFormat.format(messageDate)
        } else if (isYesterday(messageDate, currentDate)) {
            "Hôm qua " + timeFormat.format(messageDate)
        } else if (isSameYear(messageDate, currentDate)) {
            dateTimeFormat.format(messageDate)
        } else {
            dateFormat.format(messageDate) + " " + timeFormat.format(messageDate)
        }
    }

    // Hàm kiểm tra cùng ngày
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Hàm kiểm tra ngày hôm qua
    private fun isYesterday(date: Date, currentDate: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date
        cal2.time = currentDate

        cal2.add(Calendar.DAY_OF_YEAR, -1) // Trừ 1 ngày từ ngày hiện tại

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // Hàm kiểm tra cùng năm
    private fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }
}

