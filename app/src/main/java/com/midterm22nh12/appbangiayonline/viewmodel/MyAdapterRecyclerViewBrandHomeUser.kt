package com.midterm22nh12.appbangiayonline.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemBrandHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHomeUser

class MyAdapterRecyclerViewBrandHomeUser(
    private val itemList: List<ItemRecyclerViewBrandHomeUser>
) : RecyclerView.Adapter<MyAdapterRecyclerViewBrandHomeUser.MyViewHolderBrandHome>() {

    class MyViewHolderBrandHome(val binding: ItemBrandHomeUserBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderBrandHome {
        val binding = ItemBrandHomeUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolderBrandHome(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolderBrandHome, position: Int) {
        val item = itemList[position]
        holder.binding.ibBrandHome.setBackgroundResource(item.image)
    }

    override fun getItemCount() = itemList.size
}
