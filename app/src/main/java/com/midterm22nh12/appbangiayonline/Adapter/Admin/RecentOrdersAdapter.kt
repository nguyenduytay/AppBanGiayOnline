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

/**
 * Adapter hiển thị danh sách đơn hàng gần đây trên giao diện Admin Dashboard
 * Hiển thị thông tin tóm tắt về đơn hàng bao gồm ID, ngày đặt, tổng tiền, số sản phẩm và trạng thái
 *
 * @param context Context để truy cập tài nguyên và dịch vụ của ứng dụng
 * @param recentOrders Danh sách đơn hàng gần đây cần hiển thị
 */
class RecentOrdersAdapter(
    private val context: Context,
    private var recentOrders: List<OrderWithItems>
) : RecyclerView.Adapter<RecentOrdersAdapter.ViewHolder>() {

    /**
     * ViewHolder để lưu trữ các thành phần giao diện cho mỗi item đơn hàng
     * @param binding Binding tới layout của item đơn hàng gần đây
     */
    inner class ViewHolder(val binding: ItemRecentOrderBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Tạo một ViewHolder mới khi RecyclerView cần
     *
     * @param parent ViewGroup cha chứa ViewHolder mới
     * @param viewType Loại view (không sử dụng trong trường hợp này)
     * @return ViewHolder mới được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Hiển thị dữ liệu đơn hàng vào ViewHolder tại vị trí cụ thể
     * Bao gồm ID đơn hàng, ngày đặt, tổng tiền, số sản phẩm, trạng thái thanh toán và trạng thái đơn hàng
     *
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
    @SuppressLint("SetTextI18n")
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

    /**
     * Chuyển đổi mã trạng thái thành văn bản hiển thị cho người dùng
     *
     * @param status Mã trạng thái của đơn hàng
     * @return Văn bản mô tả trạng thái bằng tiếng Việt
     */
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

    /**
     * Lấy resource ID của màu dựa trên trạng thái đơn hàng
     *
     * @param status Mã trạng thái của đơn hàng
     * @return Resource ID của màu tương ứng với trạng thái
     */
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

    /**
     * Trả về số lượng item trong danh sách đơn hàng gần đây
     * @return Số lượng đơn hàng
     */
    override fun getItemCount(): Int = recentOrders.size

    /**
     * Cập nhật danh sách đơn hàng với dữ liệu mới
     * @param newOrders Danh sách đơn hàng mới cần hiển thị
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOrders: List<OrderWithItems>) {
        recentOrders = newOrders
        notifyDataSetChanged()
    }
}