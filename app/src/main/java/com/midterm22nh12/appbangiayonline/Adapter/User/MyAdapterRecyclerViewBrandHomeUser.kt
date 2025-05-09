package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemBrandHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewBrandHomeUser

class MyAdapterRecyclerViewBrandHomeUser(
    private var itemList: List<ItemRecyclerViewBrandHomeUser>,
    private var onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewBrandHomeUser.MyViewHolderBrandHome>() {

    // Biến theo dõi vị trí được chọn
    private var selectedPosition = 0

    // Interface cho sự kiện click
    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewBrandHomeUser, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }
    class MyViewHolderBrandHome(val binding: ItemBrandHomeUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolderBrandHome {
        val binding = ItemBrandHomeUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MyViewHolderBrandHome(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolderBrandHome, position: Int) {
        val item = itemList[position]

        // Hiển thị tên thương hiệu
        holder.binding.tvNameBrandHome.text = item.name

        // Thay đổi background dựa trên trạng thái chọn
        if(position == selectedPosition){
            holder.binding.clBrandHomeUser.setBackgroundResource(R.drawable.item_brand_home_user_click)
        } else {
            holder.binding.clBrandHomeUser.setBackgroundResource(R.drawable.item_brand_home_user)
        }

        // Tải hình ảnh từ URL sử dụng Glide
        when (item.image) {
            is Int -> {
                Glide.with(holder.itemView.context)
                    .load(item.image as Int)
                    .into(holder.binding.ivBrandHomeUser)
            }
            is String -> {
                Glide.with(holder.binding.ivBrandHomeUser.context)
                    .load(item.image)
                    .placeholder(R.drawable.baseline_close_24)
                    .error(R.drawable.baseline_close_24)
                    .into(holder.binding.ivBrandHomeUser)
            }
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                updateSelectedPosition(adapterPosition)
                onItemClickListener?.onItemClick(item, adapterPosition)
            }
        }
    }

    override fun getItemCount() = itemList.size

    // Phương thức cập nhật vị trí được chọn - đã thay đổi thành public
    fun updateSelectedPosition(position: Int) {
        if (position != selectedPosition && position in 0 until itemCount) {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newBrands: List<ItemRecyclerViewBrandHomeUser>) {
        itemList = newBrands
        selectedPosition = 0
        notifyDataSetChanged()
    }
}