package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.util.Log
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

/**
 * Adapter để hiển thị danh sách đơn hàng trong giao diện Admin
 * Cung cấp chức năng xem chi tiết, lọc theo trạng thái, và cập nhật danh sách đơn hàng
 *
 * @param orderData Danh sách đơn hàng và chi tiết mục đơn hàng
 * @param onItemDetailClick Callback khi người dùng nhấn vào nút xem chi tiết đơn hàng
 * @param onProcessOrderClick Callback khi người dùng nhấn vào nút xử lý đơn hàng
 */
class MyAdapterRecyclerViewBillAdmin(
    private var orderData: MutableList<OrderWithItems> = mutableListOf(),
    private val onItemDetailClick: (OrderWithItems) -> Unit,
    private val onProcessOrderClick: (String) -> Unit
) : RecyclerView.Adapter<MyAdapterRecyclerViewBillAdmin.ViewHolder>() {

    /**
     * ViewHolder để giữ các thành phần giao diện cho mỗi item đơn hàng
     * @param binding Binding tới layout của item đơn hàng
     */
    inner class ViewHolder(val binding: ItemListBillAdminBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var authViewModel : AuthViewModel

    /**
     * Tạo ViewHolder mới khi RecyclerView cần hiển thị một item mới
     * Khởi tạo authViewModel từ MainActivityAdmin
     *
     * @param parent ViewGroup chứa ViewHolder mới
     * @param viewType Loại view (không sử dụng trong trường hợp này)
     * @return ViewHolder mới được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListBillAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        authViewModel = (parent.context as MainActivityAdmin).provideAuthViewModel()
        return ViewHolder(binding)
    }

    /**
     * Trả về số lượng item trong danh sách đơn hàng
     * @return Số lượng đơn hàng cần hiển thị
     */
    override fun getItemCount(): Int = orderData.size

    /**
     * Gắn dữ liệu vào ViewHolder tại vị trí cụ thể
     * Hiển thị thông tin đơn hàng, trạng thái, sản phẩm và thiết lập sự kiện click
     *
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
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
            Log.d("MyAdapterRecyclerViewBillAdmin--", "UserId: ${order.userId}")
            // Quan sát kết quả
            authViewModel.userName_ById.observe(holder.itemView.context as LifecycleOwner) { pair ->
                val fetchedUserId = pair.first
                val fetchedUserName = pair.second
                // Chỉ cập nhật UI nếu userId trùng khớp với đơn hàng hiện tại
                if (fetchedUserId == order.userId) {
                    holder.binding.tvCustomerName.text = "Khách hàng: $fetchedUserName"
                    Log.d("MyAdapterRecyclerViewBillAdmin", "UserId: $fetchedUserId, UserName: $fetchedUserName")
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

    /**
     * Cập nhật danh sách đơn hàng với dữ liệu mới
     * @param newData Danh sách đơn hàng mới cần hiển thị
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<OrderWithItems>) {
        orderData.clear()
        orderData.addAll(newData)
        notifyDataSetChanged()
    }

    /**
     * Lấy danh sách đơn hàng hiện tại
     * @return Danh sách đơn hàng hiện đang được hiển thị
     */
    fun getOrderData(): List<OrderWithItems> {
        return orderData.toList()
    }

    /**
     * Lọc đơn hàng theo trạng thái cụ thể
     * Nếu status là "all", hiển thị tất cả đơn hàng
     *
     * @param status Trạng thái cần lọc (pending, processing, shipping, delivered, evaluate, all)
     */
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

    /**
     * Chuyển đổi mã trạng thái sang văn bản hiển thị
     * @param status Mã trạng thái (pending, processing, shipping, delivered, evaluate)
     * @return Văn bản trạng thái hiển thị cho người dùng
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
     * Lấy resource ID của background tương ứng với trạng thái
     * @param status Mã trạng thái (pending, processing, shipping, delivered, evaluate)
     * @return Resource ID của drawable background
     */
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