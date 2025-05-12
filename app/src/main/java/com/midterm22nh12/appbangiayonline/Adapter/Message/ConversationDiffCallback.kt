package com.midterm22nh12.appbangiayonline.Adapter.Message

import androidx.recyclerview.widget.DiffUtil
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation

// DiffUtil để cập nhật RecyclerView hiệu quả
class ConversationDiffCallback(
    private val oldList: List<Conversation>,
    private val newList: List<Conversation>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old.lastMessage == new.lastMessage &&
                old.lastMessageTime == new.lastMessageTime &&
                old.unreadByUser == new.unreadByUser &&
                old.unreadByAdmin == new.unreadByAdmin &&
                old.status == new.status
    }
}