package com.midterm22nh12.appbangiayonline.view.Admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.Adapter.Admin.MyAdapterRecyclerViewOrderDetailAdmin
import com.midterm22nh12.appbangiayonline.databinding.DialogOrderAdminBinding
import com.midterm22nh12.appbangiayonline.databinding.OrderDetailAdminBinding
import com.midterm22nh12.appbangiayonline.databinding.ItemOrderDetailAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class order_detail_admin(
    private val context: Context,
    private val binding: OrderDetailAdminBinding,
    private val orderWithItems: OrderWithItems,
    private val lifecycleOwner: LifecycleOwner
) {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var authViewModel: AuthViewModel
    private var orderId: String = ""
    private lateinit var bindingDialog : DialogOrderAdminBinding

    init {
        setUpView()
        displayOrderDetails(orderWithItems)
        setupListeners()
    }

    private fun setUpView(){
        binding.btnBack.setOnClickListener {
            // Ẩn giao diện danh sách tin nhắn
            binding.root.visibility = View.GONE
            (context as MainActivityAdmin).loadOrders()
            // Quay lại trang trước
            (context as MainActivityAdmin).returnToPreviousOverlay()
        }
        bindingDialog = DialogOrderAdminBinding.inflate(LayoutInflater.from(context))
        orderViewModel = (context as MainActivityAdmin).provideOrderViewModel()
        authViewModel = (context as MainActivityAdmin).provideAuthViewModel()
        orderId = orderWithItems.order.id

        // Xử lý hiển thị danh sách sản phẩm
        if (orderWithItems.items.isNotEmpty()) {
                setupProductsRecyclerView()
                binding.tvMoreProducts.visibility = View.GONE // Ẩn đi vì chúng ta hiển thị tất cả trong RecyclerView
        }
        loadOrderDetail()
    }

    private fun setupProductsRecyclerView() {
        val adapter = MyAdapterRecyclerViewOrderDetailAdmin(context, orderWithItems.items)
        binding.rvListProduct.layoutManager = LinearLayoutManager(context)
        binding.rvListProduct.adapter = adapter
    }

    private fun setupListeners() {
        // Nút cập nhật trạng thái
        binding.btnUpdateStatus.setOnClickListener {
            showProcessOrderDialog(orderId)
        }

        // Nút cập nhật thanh toán
        binding.btnUpdatePayment.setOnClickListener {
            showUpdatePaymentDialog()
        }

        // Nút hủy đơn hàng
        binding.btnCancelOrder.setOnClickListener {
            showCancelDialog()
        }

        // Nút in đơn hàng (chức năng mở rộng)
        binding.btnPrintOrder.setOnClickListener {
            Toast.makeText(context, "Tính năng in đơn hàng đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayOrderDetails(orderWithItems: OrderWithItems) {
        val order = orderWithItems.order
        val items = orderWithItems.items

        // Format tiền tệ
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        // Format ngày giờ
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        // Hiển thị thông tin đơn hàng
        binding.apply {
            // Lấy 8 ký tự đầu tiên của ID đơn hàng
            tvOrderId.text = "Mã đơn: #${order.id.take(8)}"
            tvOrderDate.text = dateFormat.format(Date(order.createdAt))

            // Hiển thị trạng thái đơn hàng từ OrderItem đầu tiên
            val status = if (items.isNotEmpty()) items[0].status else "pending"
            tvOrderStatus.text = getStatusText(status)
            tvOrderStatus.setTextColor(getStatusColor(status))

            // Thông tin khách hàng
            authViewModel.getUserNameById(order.userId)
            authViewModel.userName_ById.observe(lifecycleOwner) { pair ->
                val fetchedUserId = pair.first
                val fetchedUserName = pair.second
                // Chỉ cập nhật UI nếu userId trùng khớp với đơn hàng hiện tại
                if (fetchedUserId == order.userId) {
                    tvCustomerName.text = fetchedUserName
                }
            }
            tvCustomerPhone.text = order.phoneNumber
            tvShippingAddress.text = order.shippingAddress

            // Thông tin thanh toán
            tvPaymentMethod.text = when (order.paymentMethod) {
                "COD" -> "Thanh toán khi nhận hàng (COD)"
                "banking" -> "Chuyển khoản ngân hàng"
                "card" -> "Thẻ tín dụng/ghi nợ"
                else -> order.paymentMethod
            }

            // Trạng thái thanh toán
            tvPaymentStatus.text = when (order.paymentStatus) {
                "paid" -> "Đã thanh toán"
                "unpaid" -> "Chưa thanh toán"
                "refunded" -> "Đã hoàn tiền"
                else -> "Không xác định"
            }
            tvPaymentStatus.setTextColor(
                context.resources.getColor(
                    when (order.paymentStatus) {
                        "paid" -> android.R.color.holo_green_dark
                        "refunded" -> android.R.color.holo_green_dark
                        else -> android.R.color.holo_red_light
                    }
                )
            )

            // Tính toán chi phí
            val subtotal = items.sumOf { it.price * it.quantity }
            tvSubtotal.text = "${numberFormat.format(subtotal)} đ"
            tvShippingFee.text = "${numberFormat.format(order.shippingFee)} đ"

            // Hiển thị giảm giá nếu có
            if (order.discount > 0) {
                layoutDiscount.visibility = View.VISIBLE
                tvDiscount.text = "-${numberFormat.format(order.discount)} đ"
            } else {
                layoutDiscount.visibility = View.VISIBLE
                tvDiscount.text = "0 đ"
            }

            // Tổng cộng
            tvTotalAmount.text = "${numberFormat.format(order.totalAmount)} đ"

            // Hiển thị ghi chú nếu có
            if (order.note.isNotEmpty()) {
                cardNote.visibility = View.VISIBLE
                tvNote.text = order.note
            } else {
                cardNote.visibility = View.GONE
            }

            // Ẩn/hiện nút hủy đơn hàng dựa vào trạng thái
            btnCancelOrder.visibility = when (status) {
                "cancelled", "delivered", "evaluate" -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProcessOrderDialog(orderId: String) {
        // Tạo dialog
        val dialogBinding = DialogOrderAdminBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        // Thiết lập thông tin dialog
        dialogBinding.tvOrderIdProcess.text = "Mã đơn hàng: #${orderId.take(8)}"

        // Lấy danh sách các item hiện tại
        val items = orderWithItems.items

        if (items.isNotEmpty()) {
            val currentStatus = items[0].status

            // Cập nhật UI timeline và RadioButton trong dialog
            updateStatusProgress(dialogBinding, currentStatus)

            // Thiết lập sự kiện cho RadioButton để cập nhật timeline khi chọn trạng thái mới
            setupRadioButtonListeners(dialogBinding)

            // Thiết lập sự kiện khi nhấn nút
            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btnSave.setOnClickListener {
                // Xác định trạng thái mới từ RadioGroup
                val newStatus = getSelectedStatus(dialogBinding)
                val note = dialogBinding.etNote.text.toString().trim()

                // Kiểm tra xem đã chọn trạng thái mới chưa
                if (newStatus.isEmpty()) {
                    Toast.makeText(context, "Vui lòng chọn trạng thái mới", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Cập nhật trạng thái cho tất cả các OrderItem
                for (item in items) {
                    orderViewModel.updateOrderStatus(orderWithItems.order.id,item.id, newStatus)
                }

                dialog.dismiss()
                Toast.makeText(context, "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show()

                // Tải lại chi tiết đơn hàng
                loadOrderDetail()
            }
        }

        dialog.show()
    }

    // Thiết lập sự kiện cho RadioButton
    private fun setupRadioButtonListeners(binding: DialogOrderAdminBinding) {
        bindingDialog.rgOrderStatus.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbStatusPending -> {

                    updateTimelineUI(binding, "pending")
                }
                R.id.rbStatusProcessing -> {
                    updateTimelineUI(binding, "processing")
                }
                R.id.rbStatusShipping -> {
                    updateTimelineUI(binding, "shipping")
                }
                R.id.rbStatusDelivered -> {
                    updateTimelineUI(binding, "delivered")
                }
            }
        }
    }

    // Cập nhật UI timeline dựa trên trạng thái
    private fun updateTimelineUI(binding: DialogOrderAdminBinding, status: String) {
        // Reset tất cả icon về trạng thái mặc định
        binding.ivStatusPending.setImageResource(R.drawable.ic_circle)
        binding.ivStatusProcessing.setImageResource(R.drawable.ic_circle)
        binding.ivStatusShipping.setImageResource(R.drawable.ic_circle)
        binding.ivStatusDelivered.setImageResource(R.drawable.ic_circle)

        binding.divider1.setBackgroundColor(context.resources.getColor(R.color.grey_300, null))
        binding.divider2.setBackgroundColor(context.resources.getColor(R.color.grey_300, null))
        binding.divider3.setBackgroundColor(context.resources.getColor(R.color.grey_300, null))

        // Cập nhật UI dựa trên trạng thái được chọn
        when (status) {
            "pending" -> {
                binding.ivStatusPending.setImageResource(R.drawable.ic_circle_check)
            }
            "processing" -> {
                binding.ivStatusPending.setImageResource(R.drawable.ic_circle)
                binding.ivStatusProcessing.setImageResource(R.drawable.ic_circle_check)
                binding.divider1.setBackgroundColor(context.resources.getColor(R.color.black, null))
            }
            "shipping" -> {
                binding.ivStatusPending.setImageResource(R.drawable.ic_circle)
                binding.ivStatusProcessing.setImageResource(R.drawable.ic_circle)
                binding.ivStatusShipping.setImageResource(R.drawable.ic_circle_check)
                binding.divider1.setBackgroundColor(context.resources.getColor(R.color.black, null))
                binding.divider2.setBackgroundColor(context.resources.getColor(R.color.black, null))
            }
            "delivered", "evaluate" -> {
                binding.ivStatusPending.setImageResource(R.drawable.ic_circle)
                binding.ivStatusProcessing.setImageResource(R.drawable.ic_circle)
                binding.ivStatusShipping.setImageResource(R.drawable.ic_circle)
                binding.ivStatusDelivered.setImageResource(R.drawable.ic_circle_check)
                binding.divider1.setBackgroundColor(context.resources.getColor(R.color.black, null))
                binding.divider2.setBackgroundColor(context.resources.getColor(R.color.black, null))
                binding.divider3.setBackgroundColor(context.resources.getColor(R.color.black, null))
            }
        }
    }

    // Cập nhật UI timeline và RadioButton dựa trên trạng thái hiện tại
    private fun updateStatusProgress(binding: DialogOrderAdminBinding, currentStatus: String) {
        // Cập nhật timeline UI
        updateTimelineUI(binding, currentStatus)

        // Chọn RadioButton tương ứng với trạng thái hiện tại
        when (currentStatus) {
            "pending" -> binding.rbStatusPending.isChecked = true
            "processing" -> binding.rbStatusProcessing.isChecked = true
            "shipping" -> binding.rbStatusShipping.isChecked = true
            "delivered", "evaluate" -> binding.rbStatusDelivered.isChecked = true
        }
    }

    // Lấy trạng thái được chọn từ RadioGroup
    private fun getSelectedStatus(binding: DialogOrderAdminBinding): String {
        return when {
            binding.rbStatusPending.isChecked -> "pending"
            binding.rbStatusProcessing.isChecked -> "processing"
            binding.rbStatusShipping.isChecked -> "shipping"
            binding.rbStatusDelivered.isChecked -> "delivered"
            else -> "" // Trường hợp không có trạng thái nào được chọn
        }
    }
    private fun showUpdatePaymentDialog() {
        val options = arrayOf("Chưa thanh toán", "Đã thanh toán", "Đã hoàn tiền")

        // Lấy trạng thái thanh toán hiện tại
        val currentPaymentStatus = orderWithItems.order.paymentStatus

        val selectedIndex = when (currentPaymentStatus) {
            "paid" -> 1
            "refunded" -> 2
            else -> 0
        }

        AlertDialog.Builder(context)
            .setTitle("Cập nhật trạng thái thanh toán")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val newPaymentStatus = when (which) {
                    1 -> "paid"
                    2 -> "refunded"
                    else -> "unpaid"
                }

                orderViewModel.updatePaymentStatus(orderId, newPaymentStatus)
                dialog.dismiss()
                Toast.makeText(context, "Đã cập nhật trạng thái thanh toán", Toast.LENGTH_SHORT).show()

                // Tải lại chi tiết đơn hàng
                loadOrderDetail()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(context)
            .setTitle("Hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?")
            .setPositiveButton("Xác nhận") { _, _ ->
                // Lấy danh sách các OrderItem từ đơn hàng hiện tại
                val items = orderWithItems.items

                // Cập nhật trạng thái hủy cho tất cả các OrderItem
                for (item in items) {
                    orderViewModel.updateOrderStatus(orderWithItems.order.id,item.id, "cancelled")
                }

                Toast.makeText(context, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show()

                // Tải lại chi tiết đơn hàng
                loadOrderDetail()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

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

    private fun getStatusColor(status: String): Int {
        return context.resources.getColor(
            when (status) {
                "pending" -> android.R.color.holo_orange_light
                "processing" -> android.R.color.holo_blue_light
                "shipping" -> android.R.color.holo_blue_dark
                "delivered", "evaluate" -> android.R.color.holo_green_dark
                "cancelled" -> android.R.color.holo_red_light
                else -> android.R.color.darker_gray
            }
        )
    }

    private fun loadOrderDetail() {
        // Tải lại thông tin đơn hàng
        orderViewModel.getOrderDetail(orderId)
        orderViewModel.orderDetailState.observe(lifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val updatedOrder = state.data
                    displayOrderDetails(updatedOrder)
                }
                is UiState.Loading -> {
                    // Hiển thị loading nếu cần
                }
                is UiState.Error -> {
                    Toast.makeText(context, "Lỗi: $state", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}