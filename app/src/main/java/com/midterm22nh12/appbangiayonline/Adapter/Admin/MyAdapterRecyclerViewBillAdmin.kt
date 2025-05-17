package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemListBillAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.view.Admin.MainActivityAdmin
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdapterRecyclerViewBillAdmin(
    private var orderData: MutableList<OrderWithItems> = mutableListOf(),
    private val onItemDetailClick: (OrderWithItems) -> Unit,
    private val onProcessOrderClick: (String) -> Unit
) : RecyclerView.Adapter<MyAdapterRecyclerViewBillAdmin.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListBillAdminBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var authViewModel : AuthViewModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListBillAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        authViewModel = (parent.context as MainActivityAdmin).provideAuthViewModel()
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = orderData.size

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderWithItems = orderData[position]
        val order = orderWithItems.order
        val items = orderWithItems.items

        // Format tiền tệ
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        // Format ngày giờ
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Lấy trạng thái từ item đầu tiên trong danh sách (nếu có)
        val status = if (items.isNotEmpty()) items[0].status else "pending"

        // Thiết lập thông tin cơ bản
        holder.binding.apply {
            // Lấy 8 ký tự đầu tiên của ID đơn hàng
            tvOrderId.text = "Mã đơn: #${order.id.take(8)}"
            authViewModel.getUserNameById(order.userId)
            // Quan sát kết quả
            authViewModel.userName_ById.observe(holder.itemView.context as LifecycleOwner) { pair ->
                val fetchedUserId = pair.first
                val fetchedUserName = pair.second
                // Chỉ cập nhật UI nếu userId trùng khớp với đơn hàng hiện tại
                if (fetchedUserId == order.userId) {
                    holder.binding.tvCustomerName.text = "Khách hàng: $fetchedUserName"
                }
            }
            tvOrderDate.text = "Ngày đặt: ${dateFormat.format(Date(order.createdAt))}"

            // Thiết lập trạng thái đơn hàng
            tvStatus.text = getStatusText(status)
            tvStatus.background = root.context.getDrawable(getStatusBackground(status))

            // Hiển thị thông tin sản phẩm đầu tiên (nếu có)
            if (items.isNotEmpty()) {
                val firstItem = items.first()

                // Tên sản phẩm và số lượng sản phẩm còn lại
                tvProductCount.text = if (items.size > 1) {
                    "${firstItem.productName} và ${items.size - 1} sản phẩm khác"
                } else {
                    firstItem.productName
                }

                // Hình ảnh sản phẩm
                Glide.with(ivProductThumbnail.context)
                    .load(firstItem.productImage)
                    .placeholder(R.drawable.shoes)
                    .error(R.drawable.shoes)
                    .centerCrop()
                    .into(ivProductThumbnail)
            } else {
                // Trường hợp không có sản phẩm nào (hiếm gặp)
                tvProductCount.text = "Không có sản phẩm"
                ivProductThumbnail.setImageResource(R.drawable.shoes)
            }

            // Tổng tiền đơn hàng
            tvTotalAmount.text = "${numberFormat.format(order.totalAmount)} đ"

            // Phương thức thanh toán
            tvPaymentMethod.text = "Thanh toán: ${
                when (order.paymentMethod) {
                    "COD" -> "COD"
                    "banking" -> "Chuyển khoản"
                    "card" -> "Thẻ tín dụng"
                    else -> order.paymentMethod
                }
            }"

            // Trạng thái thanh toán
            tvPaymentStatus.text = when (order.paymentStatus) {
                "paid" -> "Đã thanh toán"
                "unpaid" -> "Chưa thanh toán"
                "refunded" -> "Đã hoàn tiền"
                else -> "Không xác định"
            }
            tvPaymentStatus.background = root.context.getDrawable(
                when (order.paymentStatus) {
                    "paid" -> R.drawable.bg_payment_paid
                    "refunded" -> R.drawable.bg_payment_paid
                    else -> R.drawable.bg_payment_unpaid
                }
            )

            // Xử lý sự kiện click
            btnViewDetail.setOnClickListener {
                onItemDetailClick(orderData[position])
            }

        }
    }

    // Cập nhật dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<OrderWithItems>) {
        orderData.clear()
        orderData.addAll(newData)
        notifyDataSetChanged()
    }

    // Lấy dữ liệu hiện tại
    fun getOrderData(): List<OrderWithItems> {
        return orderData.toList()
    }

    // Lọc đơn hàng theo trạng thái
    @SuppressLint("NotifyDataSetChanged")
    fun filterByStatus(status: String) {
        val allData = orderData.toList() // Tạo bản sao

        val filteredData = if (status == "all") {
            allData
        } else {
            allData.filter { orderWithItems ->
                orderWithItems.items.isNotEmpty() && orderWithItems.items[0].status == status
            }
        }

        orderData.clear()
        orderData.addAll(filteredData)
        notifyDataSetChanged()
    }

    // Helper methods để lấy text và background cho trạng thái
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
            "evaluate" -> R.drawable.bg_messager_user
            else -> R.drawable.bg_status_pending
        }
    }
}