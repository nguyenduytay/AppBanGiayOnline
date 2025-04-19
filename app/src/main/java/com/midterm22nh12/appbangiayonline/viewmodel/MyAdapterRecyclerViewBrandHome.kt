package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemRecyclerViewBrandHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHome
class MyAdapterRecyclerViewBrandHome(private val itemList : List<ItemRecyclerViewBrandHome>)
    : RecyclerView.Adapter<MyAdapterRecyclerViewBrandHome.MyViewHolderBrandHome>()
{
    private lateinit var bindingItemRecyclerBrandHomeUser : ItemRecyclerViewBrandHomeUserBinding
    class MyViewHolderBrandHome(val binding: ItemRecyclerViewBrandHomeUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderBrandHome {
        bindingItemRecyclerBrandHomeUser= ItemRecyclerViewBrandHomeUserBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        return MyViewHolderBrandHome(bindingItemRecyclerBrandHomeUser)
    }

    override fun onBindViewHolder(holder: MyViewHolderBrandHome, position: Int) {
        // Glide.with(bindingItemRecyclerViewNotificationHome.ivNotification.context).load(itemList.imageUrl).into(imageViewItem)
        bindingItemRecyclerBrandHomeUser.ibBrandHome.setBackgroundResource(itemList[position].image)
    }

    override fun getItemCount() = itemList.size
}