package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemNotificationHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewNotificationHomeUser

class MyAdapterRecyclerViewNotificationHomeUser(private val itemList : List<ItemRecyclerViewNotificationHomeUser>)
    :RecyclerView.Adapter<MyAdapterRecyclerViewNotificationHomeUser.MyViewHolder>()
{
    private lateinit var bindingItemNotificationHome : ItemNotificationHomeUserBinding
    class MyViewHolder(val binding: ItemNotificationHomeUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemNotificationHome=ItemNotificationHomeUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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