package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemTypeProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewTypeProductHomeUser

class MyAdapterRecyclerViewTypeProductHomeUser(
    private var itemList: List<ItemRecyclerViewTypeProductHomeUser> = emptyList(),
    private var onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewTypeProductHomeUser.MyViewHolder>() {

    // Biến theo dõi vị trí được chọn
    private var selectedPosition = 0

    // Interface cho sự kiện click
    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewTypeProductHomeUser, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }
    class MyViewHolder(val binding: ItemTypeProductHomeUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTypeProductHomeUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]

        // Hiển thị tên loại sản phẩm
        holder.binding.tvNameTypeProductHome.text = item.name

        // Thay đổi background dựa trên trạng thái chọn
        if(position == selectedPosition){
            holder.binding.tvNameTypeProductHome.setBackgroundResource(R.drawable.item_brand_home_user_click)
        } else {
            holder.binding.tvNameTypeProductHome.setBackgroundResource(R.drawable.item_brand_home_user)
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

    // Phương thức cập nhật vị trí được chọn - đã thay đổi thành public
    fun updateSelectedPosition(position: Int) {
        if (position != selectedPosition && position in 0 until itemCount) {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(position)
        }
    }

    // Phương thức cập nhật dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<ItemRecyclerViewTypeProductHomeUser>) {
        itemList = newItems
        selectedPosition = 0
        notifyDataSetChanged()
    }
}