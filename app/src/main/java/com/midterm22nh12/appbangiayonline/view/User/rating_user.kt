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
import com.midterm22nh12.appbangiayonline.databinding.RatingUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel


class rating_user(private val context: Context, private val binding: RatingUserBinding) {
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var pendingAdapter: MyAdapterRecyclerViewConfirmation
    private var userId: String = ""
    init {
        setUpView()
    }

    private fun setUpView() {
        binding.ivBackRatingUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
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
                    (context as MainActivityUser).showEvaluateUser(item)
                }

                override fun onClickOrder(item: ItemRecyclerViewConfirmation) {
                }

                override fun onCLickDeleteOrder(item: ItemRecyclerViewConfirmation) {
                }
            }
        )

        binding.rcRatingUser.apply {
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
                    binding.progressBarRatingUser.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    // Ẩn loading
                    binding.progressBarRatingUser.visibility = View.GONE

                    // Lọc và chuyển đổi các item đang chờ
                    val pendingProducts = state.data
                        .filter { it.status == "delivered" }
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
                    binding.progressBarRatingUser.visibility = View.GONE
                    showErrorMessage(state.message)
                }
            }
        }
    }

    // Hàm hiển thị thông báo lỗi
    private fun showErrorMessage(message: String) {
        // Hiển thị thông báo lỗi, ví dụ bằng Toast hoặc Snackbar
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        Log.e("ConfirmationUserActivity", message)
    }
}