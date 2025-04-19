package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemRecyclerViewBrandHomeBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHome
class MyAdapterRecyclerViewBrandHome(private val itemList : List<ItemRecyclerViewBrandHome>)
    : RecyclerView.Adapter<MyAdapterRecyclerViewBrandHome.MyViewHolderBrandHome>()
{
    private lateinit var bindingItemRecyclerBrandHome : ItemRecyclerViewBrandHomeBinding
    class MyViewHolderBrandHome(val binding: ItemRecyclerViewBrandHomeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderBrandHome {
        bindingItemRecyclerBrandHome= ItemRecyclerViewBrandHomeBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)
        return MyViewHolderBrandHome(bindingItemRecyclerBrandHome)
    }

    override fun onBindViewHolder(holder: MyViewHolderBrandHome, position: Int) {
        // Glide.with(bindingItemRecyclerViewNotificationHome.ivNotification.context).load(itemList.imageUrl).into(imageViewItem)
        bindingItemRecyclerBrandHome.ibBrandHome.setBackgroundResource(itemList[position].image)
    }

    override fun getItemCount() = itemList.size
}