package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemNotificationHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewNotificationHomeUser

class MyAdapterRecyclerViewNotificationHomeUser(private val itemList : List<ItemRecyclerViewNotificationHomeUser>)
    :RecyclerView.Adapter<MyAdapterRecyclerViewNotificationHomeUser.MyViewHolder>()
{
    private lateinit var bindingItemRecyclerViewNotificationHome : ItemNotificationHomeUserBinding
    class MyViewHolder(private val binding: ItemNotificationHomeUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemRecyclerViewNotificationHome=ItemNotificationHomeUserBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(bindingItemRecyclerViewNotificationHome)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        bindingItemRecyclerViewNotificationHome.tvTitleNotificationHome.text = itemList[position].title
        bindingItemRecyclerViewNotificationHome.tvContentNotificationHome.text = itemList[position].content
        // Glide.with(bindingItemRecyclerViewNotificationHome.ivNotification.context).load(itemList.imageUrl).into(imageViewItem)
        bindingItemRecyclerViewNotificationHome.ivNotification.setBackgroundResource(itemList[position].image)
    }

    override fun getItemCount() = itemList.size
}