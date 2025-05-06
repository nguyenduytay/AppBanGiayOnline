package com.midterm22nh12.appbangiayonline.Adapter.User

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemNotificationProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewNotificationProductHomeUser

class MyAdapterRecyclerViewNotificationProductHomeUser(private val itemList: List<ItemRecyclerViewNotificationProductHomeUser>) :
    RecyclerView.Adapter<MyAdapterRecyclerViewNotificationProductHomeUser.MyViewHolder>() {
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
        bindingItemNotificationHome.tvTitleNotificationHome.text = itemList[position].title
        bindingItemNotificationHome.tvContentNotificationHome.text = itemList[position].content
        // Glide.with(bindingItemRecyclerViewNotificationHome.ivNotification.context).load(itemList.imageUrl).into(imageViewItem)
        bindingItemNotificationHome.ivNotification.setBackgroundResource(itemList[position].image)
    }

    override fun getItemCount() = itemList.size
}