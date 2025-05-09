package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemColorsUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewColorOrderUser

class MyAdapterRecyclerViewColorOrderView(
    private var itemList: List<ItemRecyclerViewColorOrderUser> = emptyList(),
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewColorOrderView.MyViewHolder>() {

    // Biến theo dõi vị trí được chọn
    private var selectedPosition = 0

    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewColorOrderUser, position: Int)
    }

    class MyViewHolder(val binding: ItemColorsUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemColorsUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]

        // Tải ảnh sử dụng Glide
        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.baseline_close_24)
            .error(R.drawable.baseline_close_24)
            .into(holder.binding.ivColorUser)
        // Thiết lập trạng thái được chọn
        if (position == selectedPosition) {
           holder.binding.root.setBackgroundResource(R.drawable.item_border_account)
        } else {
           holder.binding.root.setBackgroundResource(R.drawable.item_border_account_no_click)
        }

        // Thiết lập sự kiện click
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                updateSelectedPosition(adapterPosition)
                onItemClickListener?.onItemClick(item, adapterPosition)
            }
        }
    }

    // Phương thức cập nhật vị trí được chọn
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
    fun updateData(newItems: List<ItemRecyclerViewColorOrderUser>) {
        itemList = newItems
        selectedPosition = 0
        notifyDataSetChanged()
    }

    // Phương thức lấy vị trí đang được chọn
    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}