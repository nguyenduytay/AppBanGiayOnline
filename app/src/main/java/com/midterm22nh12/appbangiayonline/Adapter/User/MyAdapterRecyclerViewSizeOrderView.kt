package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemSizeOrderUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewSizeOrderView

class MyAdapterRecyclerViewSizeOrderView(
    private var itemList: List<ItemRecyclerViewSizeOrderView> = emptyList(),
    private val onItemClickListener: OnItemClickListener? = null
) : RecyclerView.Adapter<MyAdapterRecyclerViewSizeOrderView.MyViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    // Interface cho sự kiện click
    interface OnItemClickListener {
        fun onItemClick(item: ItemRecyclerViewSizeOrderView, position: Int)
    }

    private lateinit var bindingItemSizeOrderView: ItemSizeOrderUserBinding

    class MyViewHolder(val binding: ItemSizeOrderUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        bindingItemSizeOrderView = ItemSizeOrderUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(bindingItemSizeOrderView)
    }

    override fun getItemCount() = itemList.size

    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = itemList[position]

        // Hiển thị kích thước
        holder.binding.tvSize.text = item.size

        // Change the background based on selection state
        if (position == selectedPosition) {
            // Selected item style
            holder.binding.tvSize.setBackgroundResource(R.drawable.item_border_account)
        } else {
            // Unselected item style
            holder.binding.tvSize.setBackgroundResource(R.drawable.item_border_account_no_click)
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener {
            // Update the selected position
            val previousSelected = selectedPosition
            selectedPosition = position

            // Notify adapter to redraw the previously selected and newly selected items
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)

            // Call the listener
            onItemClickListener?.onItemClick(item, position)
        }
    }

    // Phương thức cập nhật dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<ItemRecyclerViewSizeOrderView>) {
        itemList = newItems
        selectedPosition = 0
        notifyDataSetChanged()
    }
}