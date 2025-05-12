package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class MyAdapterRecyclerViewProductHomeUser(
    private var itemList: List<ItemRecyclerViewProductHomeUser>,
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewProductHomeUser.MyViewHolderProductHome>() {

    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewProductHomeUser, position: Int)
        fun onFavoriteClick(item: ItemRecyclerViewProductHomeUser, position: Int)
    }

    class MyViewHolderProductHome(val binding: ItemProductHomeUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderProductHome {
        val binding = ItemProductHomeUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolderProductHome(binding)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: MyViewHolderProductHome, position: Int) {
        try {
            if (position < 0 || position >= itemList.size) return

            val item = itemList[position]
            val imageUrl = item.colors.firstOrNull()?.image ?: ""
            try {
                Glide.with(holder.binding.ivImageProductHomeUser.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.shoes1)
                    .error(R.drawable.shoes1)
                    .into(holder.binding.ivImageProductHomeUser)
            } catch (e: Exception) {
                Log.e("Adapter", "Error loading image: ${e.message}")
                holder.binding.ivImageProductHomeUser.setImageResource(R.drawable.shoes1)
            }
            holder.binding.tvEvaluateProductHomeUser.text = item.rating.toString()
            holder.binding.tvPriceProductHomeUser.text = String.format("%,d vnđ", item.price)

            holder.binding.tvNameProductHomeUser.text = item.name
            holder.itemView.setOnClickListener {
                try {
                    onItemClickListener?.onItemClick(item, position)
                } catch (e: Exception) {
                    Log.e("Adapter", "Error on item click: ${e.message}")
                }
            }
            holder.binding.ivLiveProductHomeUser.setOnClickListener {
                try {
                    onItemClickListener?.onFavoriteClick(item, position)
                } catch (e: Exception) {
                    Log.e("Adapter", "Error on favorite click: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("Adapter", "Error in onBindViewHolder: ${e.message}")
        }
    }

    override fun getItemCount() = itemList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newProducts: List<ItemRecyclerViewProductHomeUser>) {
        try {
            itemList = newProducts
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("Adapter", "Error updating data: ${e.message}")
        }
    }
}