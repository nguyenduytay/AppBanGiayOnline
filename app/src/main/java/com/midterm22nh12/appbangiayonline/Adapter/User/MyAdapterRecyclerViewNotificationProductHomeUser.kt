package com.midterm22nh12.appbangiayonline.Adapter.User

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemNotificationProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewNotificationProductHomeUser

class MyAdapterRecyclerViewNotificationProductHomeUser(
    private val itemList: List<ItemRecyclerViewNotificationProductHomeUser>,
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewNotificationProductHomeUser.MyViewHolder>() {

    // Interface để xử lý sự kiện click
    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewNotificationProductHomeUser, position: Int)
    }

    private lateinit var bindingItemNotificationHome: ItemNotificationProductHomeUserBinding

    class MyViewHolder(val binding: ItemNotificationProductHomeUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemNotificationHome = ItemNotificationProductHomeUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bindingItemNotificationHome)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]

        holder.binding.tvTitleNotificationHome.text = currentItem.title
        holder.binding.tvContentNotificationHome.text = currentItem.content
        // Glide.with(holder.binding.ivNotification.context).load(currentItem.imageUrl).into(holder.binding.ivNotification)
        holder.binding.ivNotification.setBackgroundResource(currentItem.image)

        // Thiết lập sự kiện click cho item
        holder.binding.root.setOnClickListener {
            onItemClickListener?.onItemClick(currentItem, position)
        }
    }

    override fun getItemCount() = itemList.size
}