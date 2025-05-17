    package com.midterm22nh12.appbangiayonline.view.User

    import android.content.Context
    import android.util.Log
    import android.view.View
    import android.widget.Toast
    import androidx.lifecycle.LifecycleOwner
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.google.firebase.auth.FirebaseAuth
    import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewConfirmation
    import com.midterm22nh12.appbangiayonline.R
    import com.midterm22nh12.appbangiayonline.Utils.UiState
    import com.midterm22nh12.appbangiayonline.databinding.OrderConfirmationUserBinding
    import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
    import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
    import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel

    class confirmation_user(
        private val context: Context,
        private val binding: OrderConfirmationUserBinding
    ) {
        private lateinit var orderViewModel: OrderViewModel
        private lateinit var authViewModel: AuthViewModel
        private lateinit var pendingAdapter: MyAdapterRecyclerViewConfirmation
        private var userId: String = ""

        init {
            setUpView()
        }

        private fun setUpView() {
            // Thiết lập nút quay lại
            binding.ivBackOrderConfirmationUser.setOnClickListener {
                binding.root.visibility = View.GONE
                (context as MainActivityUser).returnToPreviousOverlay()
            }

            // Lấy ViewModels từ activity
            orderViewModel = (context as MainActivityUser).getSharedOrderViewModel()
            authViewModel = (context as MainActivityUser).getSharedViewModel()

            // Thiết lập RecyclerView
            setupRecyclerView()
            val firebaseAuth = FirebaseAuth.getInstance()
            userId = firebaseAuth.currentUser?.uid ?: ""

            showPendingOrders(userId)
        }

        private fun setupRecyclerView() {
            pendingAdapter = MyAdapterRecyclerViewConfirmation(
                listener = object : MyAdapterRecyclerViewConfirmation.OnItemClickListener {
                    override fun onClickReview(item: ItemRecyclerViewConfirmation) {
                    }

                    override fun onClickOrder(item: ItemRecyclerViewConfirmation) {
                    }

                    override fun onCLickDeleteOrder(item: ItemRecyclerViewConfirmation) {
                        showCancelOrderDialog(item)
                    }
                }
            )

            binding.rcListOrderConfirmationUser.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = pendingAdapter
            }
        }

        private fun showPendingOrders(userId: String) {
            // Thay đổi từ getUserOrders sang observePendingOrderItems
            orderViewModel.observePendingOrderItems(userId)

            // Quan sát trạng thái đơn hàng
            orderViewModel.pendingOrderItemsState.observe(context as LifecycleOwner) { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Hiển thị loading
                        binding.progressBarOrderConfirmation.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        // Ẩn loading
                        binding.progressBarOrderConfirmation.visibility = View.GONE

                        // Lọc và chuyển đổi các item đang chờ
                        val pendingProducts = state.data
                            .filter { it.status == "pending" || it.status == "processing" }
                            .map { orderItem ->
                                ItemRecyclerViewConfirmation(
                                    orderItemId = orderItem.orderItemId,
                                    orderId = orderItem.orderId,
                                    productId = orderItem.productId,
                                    productName = orderItem.productName,
                                    price = orderItem.price,
                                    quantity = orderItem.quantity,
                                    colorName = orderItem.colorName,
                                    size = orderItem.size,
                                    productImage = orderItem.productImage,
                                    orderDate = System.currentTimeMillis(), // Hoặc lấy từ order nếu có
                                    status = orderItem.status
                                )
                            }

                        Log.d("ConfirmationUserActivity", "Pending Products: ${pendingProducts.size}")

                        // Cập nhật adapter
                        pendingAdapter.updateData(pendingProducts)

                    }
                    is UiState.Error -> {
                        // Ẩn loading và hiển thị lỗi
                        binding.progressBarOrderConfirmation.visibility = View.GONE
                        showErrorMessage(state.message)
                    }
                }
            }
        }

        private fun showCancelOrderDialog(item: ItemRecyclerViewConfirmation) {
            // Sử dụng MaterialAlertDialogBuilder để tạo dialog đẹp và hiện đại
            MaterialAlertDialogBuilder(context)
                .setTitle("Xác nhận hủy đơn hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này?\n\n" +
                        "Mã đơn hàng: ${item.orderId}\n" +
                        "Sản phẩm: ${item.productName}")
                .setIcon(R.drawable.emoji) // Thêm icon cảnh báo
                .setPositiveButton("Đồng ý") { dialog, _ ->
                    // Xử lý hủy đơn hàng
                    item.let {
                        it.orderId?.let { it1 ->
                            item.productId?.let { it2 ->
                                item.colorName?.let { it3 ->
                                    item.quantity?.let { it4 ->
                                        orderViewModel.cancelOrderItemByProductId(
                                            orderId = it1,
                                            productId = it2,
                                            colorName = it3,
                                            index = (-it4)
                                        )
                                    }

                                }

                            }
                        }
                    }

                    // Hiển thị thông báo hủy thành công
                    showSuccessCancel()

                    dialog.dismiss()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    // Đóng dialog nếu người dùng không muốn hủy
                    dialog.dismiss()
                }
                .setCancelable(true) // Cho phép đóng dialog bằng cách chạm ngoài
                .show()
        }

        // Hàm hiển thị thông báo hủy đơn thành công
        private fun showSuccessCancel() {
            MaterialAlertDialogBuilder(context)
                .setTitle("Hủy đơn hàng")
                .setMessage("Đơn hàng của bạn đã được gửi yêu cầu hủy. Chúng tôi sẽ xử lý trong thời gian sớm nhất.")
                .setPositiveButton("Đóng") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        // Hàm hiển thị thông báo lỗi
        private fun showErrorMessage(message: String) {
            // Hiển thị thông báo lỗi, ví dụ bằng Toast hoặc Snackbar
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Log.e("ConfirmationUserActivity", message)
        }
    }