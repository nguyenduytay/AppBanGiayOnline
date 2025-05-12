package com.midterm22nh12.appbangiayonline.Adapter.Message

import androidx.recyclerview.widget.DiffUtil
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Message

// DiffUtil cho tin nhắn
class MessageDiffCallback(
    private val oldList: List<Message>,
    private val newList: List<Message>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old.content == new.content &&
                old.read == new.read &&
                old.timestamp == new.timestamp
    }
}