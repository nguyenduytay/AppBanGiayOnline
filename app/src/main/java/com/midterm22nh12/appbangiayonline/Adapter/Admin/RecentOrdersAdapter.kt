package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemRecentOrderBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentOrdersAdapter(
    private val context: Context,
    private var recentOrders: List<OrderWithItems>
) : RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecentOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderWithItems = recentOrders[position]
        val binding = holder.binding

        // Hiển thị ID đơn hàng
        binding.tvOrderId.text = "#${orderWithItems.order.id.take(8)}"

        // Hiển thị ngày đặt hàng
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.tvOrderDate.text = dateFormat.format(Date(orderWithItems.order.createdAt))

        // Hiển thị tổng tiền
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        binding.tvOrderTotal.text = "${numberFormat.format(orderWithItems.order.totalAmount)} đ"

        // Hiển thị số sản phẩm
        binding.tvItemCount.text = "${orderWithItems.items.size} sản phẩm"

        // Hiển thị trạng thái thanh toán
        binding.tvPaymentStatus.text = when (orderWithItems.order.paymentStatus) {
            "paid" -> "Đã thanh toán"
            "unpaid" -> "Chưa thanh toán"
            "refunded" -> "Đã hoàn tiền"
            else -> "Không xác định"
        }

        // Thiết lập màu cho trạng thái thanh toán
        binding.tvPaymentStatus.setTextColor(
            context.resources.getColor(
                when (orderWithItems.order.paymentStatus) {
                    "paid" -> android.R.color.holo_green_dark
                    "refunded" -> android.R.color.holo_blue_dark
                    else -> android.R.color.holo_red_light
                },
                null
            )
        )

        // Hiển thị trạng thái đơn hàng (lấy từ item đầu tiên)
        if (orderWithItems.items.isNotEmpty()) {
            val status = orderWithItems.items[0].status
            binding.tvOrderStatus.text = getStatusText(status)
            binding.tvOrderStatus.setTextColor(
                context.resources.getColor(
                    getStatusColor(status),
                    null
                )
            )
        }
    }

    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "Chờ xác nhận"
            "processing" -> "Đang xử lý"
            "shipping" -> "Đang giao hàng"
            "delivered" -> "Đã giao hàng"
            "cancelled" -> "Đã hủy"
            "evaluate" -> "Đã đánh giá"
            else -> "Không xác định"
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status) {
            "pending" -> R.color.status_pending
            "processing" -> R.color.status_processing
            "shipping" -> R.color.status_shipping
            "delivered", "evaluate" -> R.color.status_delivered
            "cancelled" -> android.R.color.holo_red_light
            else -> android.R.color.darker_gray
        }
    }

    override fun getItemCount(): Int = recentOrders.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOrders: List<OrderWithItems>) {
        recentOrders = newOrders
        notifyDataSetChanged()
    }
}