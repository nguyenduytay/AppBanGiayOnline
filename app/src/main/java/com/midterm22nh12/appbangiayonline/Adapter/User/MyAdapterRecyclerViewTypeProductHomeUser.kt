package com.midterm22nh12.appbangiayonline.Adapter.User

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.databinding.ItemTypeProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewTypeProductHomeUser

class MyAdapterRecyclerViewTypeProductHomeUser(private val itemList: List<ItemRecyclerViewTypeProductHomeUser>) :
    RecyclerView.Adapter<MyAdapterRecyclerViewTypeProductHomeUser.MyViewHolder>() {
    private lateinit var bindingItemTypeProductHomeUser: ItemTypeProductHomeUserBinding

    class MyViewHolder(val binding: ItemTypeProductHomeUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemTypeProductHomeUser= ItemTypeProductHomeUserBinding.inflate(
             LayoutInflater.from(parent.context),
             parent,
             false
         )
        return MyViewHolder(bindingItemTypeProductHomeUser)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        bindingItemTypeProductHomeUser.tvNameTypeProductHome.text = itemList[position].name
    }
}