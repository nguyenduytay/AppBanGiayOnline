
package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemOrderDetailAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import java.text.NumberFormat
import java.util.Locale

class MyAdapterRecyclerViewOrderDetailAdmin(
    private val context: Context,
    private val items: List<OrderItem>
) : RecyclerView.Adapter<MyAdapterRecyclerViewOrderDetailAdmin.OrderItemViewHolder>() {

    inner class OrderItemViewHolder(val binding: ItemOrderDetailAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderDetailAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        // Hiển thị thông tin sản phẩm
        binding.tvProductName.text = item.productName
        binding.tvProductVariant.text = "Màu: ${item.color} | Size: ${item.size}"

        // Format giá
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        binding.tvProductPrice.text = "${formatter.format(item.price)} đ"
        binding.tvProductQuantity.text = item.quantity.toString()

        // Hiển thị trạng thái
        binding.tvItemStatus.text = getStatusText(item.status)
        binding.tvItemStatus.background = context.resources.getDrawable(getStatusBackground(item.status))

        // Tải ảnh sản phẩm
        if (item.productImage.isNotEmpty()) {
            Glide.with(binding.ivProductImage.context)
                .load(item.productImage)
                .placeholder(R.drawable.shoes)
                .into(binding.ivProductImage)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "Chờ xác nhận"
            "processing" -> "Đang xử lý"
            "shipping" -> "Đang giao hàng"
            "delivered" -> "Đã giao hàng"
            "evaluate" -> "Đã đánh giá"
            else -> "Không xác định"
        }
    }

    private fun getStatusBackground(status: String): Int {
        return when (status) {
            "pending" -> R.drawable.bg_status_pending
            "processing" -> R.drawable.bg_status_processing
            "shipping" -> R.drawable.bg_status_shipped
            "delivered" -> R.drawable.bg_button_primary
            "evaluate" -> R.drawable.bg_status_processing
            else -> R.drawable.bg_status_pending_small
        }
    }
}