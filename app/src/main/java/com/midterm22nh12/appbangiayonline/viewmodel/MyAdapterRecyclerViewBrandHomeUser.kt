package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemBrandHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHomeUser
class MyAdapterRecyclerViewBrandHomeUser(private val itemList : List<ItemRecyclerViewBrandHomeUser>)
    : RecyclerView.Adapter<MyAdapterRecyclerViewBrandHomeUser.MyViewHolderBrandHome>()
{
    private lateinit var bindingItemBrandHomeUser : ItemBrandHomeUserBinding
    class MyViewHolderBrandHome(val binding: ItemBrandHomeUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderBrandHome {
        bindingItemBrandHomeUser= ItemBrandHomeUserBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        return MyViewHolderBrandHome(bindingItemBrandHomeUser)
    }

    override fun onBindViewHolder(holder: MyViewHolderBrandHome, position: Int) {
        // Glide.with(bindingItemRecyclerViewNotificationHome.ivNotification.context).load(itemList.imageUrl).into(imageViewItem)
        bindingItemBrandHomeUser.ibBrandHome.setBackgroundResource(itemList[position].image)
    }

    override fun getItemCount() = itemList.size
}