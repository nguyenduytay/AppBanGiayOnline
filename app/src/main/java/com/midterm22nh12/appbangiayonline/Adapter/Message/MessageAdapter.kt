// Sửa MessageAdapter để đảm bảo tin nhắn hiển thị đúng
package com.midterm22nh12.appbangiayonline.Adapter.Message

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemMessageSendShopUserBinding
import com.midterm22nh12.appbangiayonline.databinding.ItemMessagesReceiveShopUserBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Message
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageAdapter (
    private val messages: MutableList<Message> = mutableListOf(),
    private val currentUserId: String,
    private val isAdminView: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
        private const val TAG = "MessageAdapter"
    }

    inner class SentMessageViewHolder(private val binding: ItemMessageSendShopUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                tvMessageContentSendShopUser.text = message.content
                tvMessageTimeSendShopUser.text = formatDateTime(message.timestamp)

                // Log trạng thái đọc để debug
                Log.d(TAG, "Hiển thị tin nhắn ${message.id}, read: ${message.read}")

                // Hiển thị trạng thái đã đọc/đã gửi
                tvMessageStatusSendShopUser.visibility = View.VISIBLE

                if (message.read) {
                    tvMessageStatusSendShopUser.text = "Đã đọc"
                    tvMessageStatusSendShopUser.setTextColor(0xFF4CAF50.toInt()) // Màu xanh lá
                } else {
                    tvMessageStatusSendShopUser.text = "Đã gửi"
                    tvMessageStatusSendShopUser.setTextColor(0xFF9E9E9E.toInt()) // Màu xám
                }
            }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessagesReceiveShopUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.apply {
                tvMessageContentReceiveShopUser.text = message.content

                // Định dạng thời gian với ngày tháng
                tvMessageTimeReceiveShopUser.text = formatDateTime(message.timestamp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSendShopUserBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SentMessageViewHolder(binding)
            }

            VIEW_TYPE_RECEIVED -> {
                val binding = ItemMessagesReceiveShopUserBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        // Log để debug
        Log.d(TAG, "Getting item view type for message from ${message.senderId}")
        Log.d(TAG, "Current user ID: $currentUserId")

        return when {
            message.senderId == currentUserId ||
                    (isAdminView && message.senderId == "system") -> VIEW_TYPE_SENT
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun getItemCount() = messages.size

    // Cập nhật danh sách tin nhắn
    @SuppressLint("NotifyDataSetChanged")
    fun updateMessages(newMessages: List<Message>) {
        Log.d(TAG, "Cập nhật ${newMessages.size} tin nhắn")

        if (newMessages.isEmpty()) {
            Log.d(TAG, "Danh sách tin nhắn mới trống")
            messages.clear()
            notifyDataSetChanged()
            return
        }

        // Log chi tiết tin nhắn cho debug
        for (i in newMessages.indices) {
            Log.d(TAG, "Tin nhắn $i: ${newMessages[i].content}, " +
                    "Từ: ${newMessages[i].senderName}, " +
                    "ID: ${newMessages[i].id}, " +
                    "SenderId: ${newMessages[i].senderId}")
        }

        val diffCallback = MessageDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages.clear()
        messages.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)

        Log.d(TAG, "Đã cập nhật adapter với ${messages.size} tin nhắn")
    }

    // Thêm một tin nhắn mới
    fun addMessage(message: Message) {
        Log.d(TAG, "Thêm tin nhắn mới: ${message.content}")

        // Kiểm tra xem tin nhắn đã tồn tại chưa
        val existingIndex = messages.indexOfFirst { it.id == message.id }
        if (existingIndex >= 0) {
            Log.d(TAG, "Tin nhắn đã tồn tại, cập nhật tại vị trí $existingIndex")
            messages[existingIndex] = message
            notifyItemChanged(existingIndex)
        } else {
            Log.d(TAG, "Tin nhắn mới, thêm vào cuối danh sách")
            messages.add(message)
            notifyItemInserted(messages.size - 1)
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