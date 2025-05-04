package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class MyAdapterRecyclerViewProductHomeUser(private val itemList: List<ItemRecyclerViewProductHomeUser>) :
    RecyclerView.Adapter<MyAdapterRecyclerViewProductHomeUser.MyViewHolder>() {
    private lateinit var bindingItemProductHomeUser: ItemProductHomeUserBinding

    class MyViewHolder(val binding: ItemProductHomeUserBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemProductHomeUser = ItemProductHomeUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bindingItemProductHomeUser)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if(itemList[position].like)
            bindingItemProductHomeUser.ivLiveProductHomeUser.setImageResource(R.drawable.love1)
        else
            bindingItemProductHomeUser.ivLiveProductHomeUser.setImageResource(R.drawable.love2)
        bindingItemProductHomeUser.ivImageProductHomeUser.setImageResource(itemList[position].image)
        bindingItemProductHomeUser.tvEvaluateProductHomeUser.text = itemList[position].evaluate.toString()
        bindingItemProductHomeUser.tvPriceProductHomeUser.text = itemList[position].price.toString().plus(" vnđ")
        bindingItemProductHomeUser.tvNameProductHomeUser.text = itemList[position].name

    }

    override fun getItemCount() = itemList.size
}