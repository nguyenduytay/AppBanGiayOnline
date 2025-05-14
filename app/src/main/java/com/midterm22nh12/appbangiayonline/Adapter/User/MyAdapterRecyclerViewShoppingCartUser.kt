package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemShoppingCartUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewShoppingCartUser

class MyAdapterRecyclerViewShoppingCartUser(
    private var items: List<ItemRecyclerViewShoppingCartUser> = emptyList(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MyAdapterRecyclerViewShoppingCartUser.ViewHolder>() {

    interface OnItemClickListener {
        fun onDeleteClick(item: ItemRecyclerViewShoppingCartUser, position: Int)
        fun onQuantityChange(item: ItemRecyclerViewShoppingCartUser, position: Int, newQuantity: Int)
        fun onItemSelected(item: ItemRecyclerViewShoppingCartUser, position: Int, isSelected: Boolean)
    }

    inner class ViewHolder(val binding: ItemShoppingCartUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShoppingCartUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        Log.d("CartAdapter", "onCreateViewHolder được gọi")
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = items[position]
            with(holder.binding) {
                tvProductNameItemShoppingCartUser.text = item.productName
                tvProductPriceItemShoppingCartUser.text = String.format("%,d vnđ", item.price)
                tvQuantityItemShoppingCartUser.text = item.quantity.toString()
                tvProductColorItemShoppingCartUser.text = "Màu: ${item.colorName}"
                tvProductSizeItemShoppingCartUser.text= "Size: ${item.size}"

                // Load image với Glide
                Glide.with(holder.binding.ivProductImageItemShoppingCartUser.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.shoes)
                    .into(ivProductImageItemShoppingCartUser)
                // Set trạng thái checkbox
                cbItemShoppingCartUser.isChecked = item.isSelected

                // Thêm xử lý sự kiện khi click vào checkbox
                cbItemShoppingCartUser.setOnCheckedChangeListener { _, isChecked ->
                    item.isSelected = isChecked
                    listener.onItemSelected(item, position, isChecked)
                }

                // Xử lý sự kiện tăng số lượng
                ibAddItemShoppingCartUser.setOnClickListener {
                    if (item.quantity < item.stock) {
                        val newQuantity = item.quantity + 1
                        tvQuantityItemShoppingCartUser.text = newQuantity.toString()
                        item.quantity = newQuantity // Cập nhật giá trị trong item
                        listener.onQuantityChange(item, position, newQuantity)
                    }
                }

                // Xử lý sự kiện giảm số lượng
                ibRemoveItemShoppingCartUser.setOnClickListener {
                    if (item.quantity > 1) {
                        val newQuantity = item.quantity - 1
                        tvQuantityItemShoppingCartUser.text = newQuantity.toString()
                        item.quantity = newQuantity // Cập nhật giá trị trong item
                        listener.onQuantityChange(item, position, newQuantity)
                    }
                }
            }

            Log.d("CartAdapter", "Đã bind item: ${item.productName} tại vị trí $position")
        } catch (e: Exception) {
            Log.e("CartAdapter", "Lỗi khi bind item ở vị trí $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int {
        Log.d("CartAdapter", "getItemCount: ${items.size} items")
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<ItemRecyclerViewShoppingCartUser>) {
        Log.d("CartAdapter", "updateData được gọi với ${newItems.size} items")
        items = newItems
        notifyDataSetChanged()
    }

    // Phương thức để lấy danh sách các item đã chọn
    fun getSelectedItems(): List<ItemRecyclerViewShoppingCartUser> {
        val selectedItems = items.filter { it.isSelected }
        Log.d("CartAdapter", "getSelectedItems: ${selectedItems.size} items đã chọn")
        return selectedItems
    }

    // Phương thức để chọn/bỏ chọn tất cả
    @SuppressLint("NotifyDataSetChanged")
    fun selectAll(isSelected: Boolean) {
        Log.d("CartAdapter", "selectAll($isSelected) được gọi")
        items.forEach { it.isSelected = isSelected }
        notifyDataSetChanged()
    }
}