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

/**
 * Adapter hiển thị danh sách các mục sản phẩm trong chi tiết đơn hàng trên giao diện Admin
 *
 * @param context Context để truy cập tài nguyên và dịch vụ
 * @param items Danh sách các mục sản phẩm trong đơn hàng
 */
class MyAdapterRecyclerViewOrderDetailAdmin(
    private val context: Context,
    private val items: List<OrderItem>
) : RecyclerView.Adapter<MyAdapterRecyclerViewOrderDetailAdmin.OrderItemViewHolder>() {

    /**
     * ViewHolder để lưu trữ và quản lý các thành phần giao diện cho mỗi item
     * @param binding Binding tới layout item chi tiết đơn hàng
     */
    inner class OrderItemViewHolder(val binding: ItemOrderDetailAdminBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Tạo một ViewHolder mới khi RecyclerView cần
     *
     * @param parent ViewGroup cha chứa ViewHolder mới
     * @param viewType Loại view (không sử dụng trong trường hợp này)
     * @return ViewHolder mới được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderDetailAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    /**
     * Hiển thị dữ liệu chi tiết sản phẩm vào ViewHolder tại vị trí cụ thể
     * Bao gồm tên sản phẩm, biến thể, giá, số lượng, trạng thái và hình ảnh
     *
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
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

    /**
     * Trả về số lượng item trong danh sách
     * @return Số lượng mục sản phẩm
     */
    override fun getItemCount(): Int = items.size

    /**
     * Chuyển đổi mã trạng thái thành văn bản hiển thị người dùng có thể đọc
     *
     * @param status Mã trạng thái của mục đơn hàng
     * @return Văn bản mô tả trạng thái
     */
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

    /**
     * Lấy resource ID của background dựa trên trạng thái đơn hàng
     *
     * @param status Mã trạng thái của mục đơn hàng
     * @return Resource ID của drawable background
     */
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