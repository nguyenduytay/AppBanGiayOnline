package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class MyAdapterRecyclerViewProductHomeUser(
    private val itemList: List<ItemRecyclerViewProductHomeUser>,
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewProductHomeUser.MyViewHolder>() {

    // Interface để xử lý sự kiện click
    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewProductHomeUser, position: Int)
        fun onFavoriteClick(item: ItemRecyclerViewProductHomeUser, position: Int)
    }

    private lateinit var bindingItemProductHomeUser: ItemProductHomeUserBinding

    class MyViewHolder(val binding: ItemProductHomeUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemProductHomeUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]

        // Hiển thị trạng thái yêu thích
        if(currentItem.like)
            holder.binding.ivLiveProductHomeUser.setImageResource(R.drawable.love1)
        else
            holder.binding.ivLiveProductHomeUser.setImageResource(R.drawable.love2)

        holder.binding.ivImageProductHomeUser.setImageResource(currentItem.image)
        holder.binding.tvEvaluateProductHomeUser.text = currentItem.evaluate.toString()
        holder.binding.tvPriceProductHomeUser.text = currentItem.price.toString().plus(" vnđ")
        holder.binding.tvNameProductHomeUser.text = currentItem.name

        // Thiết lập sự kiện click cho item
        holder.binding.root.setOnClickListener {
            onItemClickListener?.onItemClick(currentItem, position)
        }

        // Thiết lập sự kiện click cho nút yêu thích
        holder.binding.ivLiveProductHomeUser.setOnClickListener {
            onItemClickListener?.onFavoriteClick(currentItem, position)
        }
    }

    override fun getItemCount() = itemList.size
}