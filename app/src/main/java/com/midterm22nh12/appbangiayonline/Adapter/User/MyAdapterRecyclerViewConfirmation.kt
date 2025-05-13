package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemOrderConfirmationEndTransportationEndMyReviewBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation

class MyAdapterRecyclerViewConfirmation(
    private var items: List<ItemRecyclerViewConfirmation> = emptyList(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MyAdapterRecyclerViewConfirmation.ViewHolder>() {

    // Interface để xử lý các sự kiện click
    interface OnItemClickListener {
        fun onDeleteDetailClick(item: ItemRecyclerViewConfirmation)
    }

    // ViewHolder để binding dữ liệu
    inner class ViewHolder(val binding: ItemOrderConfirmationEndTransportationEndMyReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderConfirmationEndTransportationEndMyReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        Log.d("OrderPendingAdapter", "onCreateViewHolder được gọi")
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = items[position]
            with(holder.binding) {
                // Hiển thị thông tin sản phẩm
                tvNameProduct.text = item.productName
                tvTotalProduct.text = String.format("%,d vnđ", item.price)
                tvSizeEndQuantityEndColorProduct.text =
                    "Màu: ${item.colorName} " + "Size: ${item.size} " + "Số lượng: ${item.quantity}"
                tvTotalProduct.text = String.format("Tổng: %,d vnđ",
                    item.price?.times(item.quantity!!) ?: 0
                )
                when (item.status) {
                    "pending" -> {
                        tvStatus.text = "Chờ xác nhận"
                        tvStatus.setTextColor(Color.parseColor("#E01C1F"))
                        btClick.setText("Hủy")
                        btClick.setBackgroundColor(Color.parseColor("#E01C1F"))
                        btClick.isEnabled = true
                    }

                    "shipping" -> {
                        tvStatus.text = "Đang vận chuyển"
                        tvStatus.setTextColor(Color.parseColor("#855555"))
                        btClick.setText("Đang vận chuyển")
                        btClick.setBackgroundColor(Color.parseColor("#855555"))
                        btClick.isEnabled = false
                    }

                    "delivered" -> {

                        tvStatus.text = "Đã giao hàng"
                        tvStatus.setTextColor(Color.parseColor("#1CE046"))
                        btClick.setText("Đánh giá")
                        btClick.setBackgroundColor(Color.parseColor("#1CE046"))
                        btClick.isEnabled = true
                    }
                }
                // Load hình ảnh sản phẩm
                Glide.with(ivImage.context)
                    .load(item.productImage)
                    .placeholder(R.drawable.shoes)
                    .into(ivImage)

                // Xử lý sự kiện nút hủy đơn
                btClick.setOnClickListener {
                    listener.onDeleteDetailClick(item)
                }
            }

            Log.d("OrderPendingAdapter", "Đã bind item: ${item.productName} tại vị trí $position")
        } catch (e: Exception) {
            Log.e("OrderPendingAdapter", "Lỗi khi bind item ở vị trí $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int {
        Log.d("OrderPendingAdapter", "getItemCount: ${items.size} items")
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<ItemRecyclerViewConfirmation>) {
        Log.d("OrderPendingAdapter", "updateData được gọi với ${newItems.size} items")
        items = newItems
        notifyDataSetChanged()
    }
}