package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.databinding.ItemRecyclerViewNotificationHomeBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewNotificationHome

class MyAdapterRecyclerViewNotificationHome(private val itemList : List<ItemRecyclerViewNotificationHome>)
    :RecyclerView.Adapter<MyAdapterRecyclerViewNotificationHome.MyViewHolder>()
{
    private lateinit var bindingItemRecyclerViewNotificationHome : ItemRecyclerViewNotificationHomeBinding
    class MyViewHolder(val binding: ItemRecyclerViewNotificationHomeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemRecyclerViewNotificationHome=ItemRecyclerViewNotificationHomeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
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