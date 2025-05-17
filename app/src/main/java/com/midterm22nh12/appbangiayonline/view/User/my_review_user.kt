package com.midterm22nh12.appbangiayonline.view.User

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewOrderReviewProductUser
import com.midterm22nh12.appbangiayonline.Service.UserService
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.databinding.OrderReviewProductUserBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.ProductReview
import com.midterm22nh12.appbangiayonline.model.Entity.User
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewOrderReviewProductUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel

class my_review_user(
    private val context: Context,
    private val binding: OrderReviewProductUserBinding,
    private val item: ItemRecyclerViewProductHomeUser
) {
    private lateinit var orderViewModel: OrderViewModel

    init {
        setUpView()
        // Gọi phương thức hiển thị đánh giá ngay khi khởi tạo
        showReview()
    }

    private fun setUpView() {
        binding.ivBackOrderMyViewUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
        orderViewModel = (context as MainActivityUser).getSharedOrderViewModel()
    }

    // Hiển thị danh sách đánh giá
    private fun showReview() {
        val productId = item.id
        Log.d("ReviewUser", "Đang lấy đánh giá cho sản phẩm ID: $productId")

        orderViewModel.getProductReviews(productId)
        orderViewModel.productReviewsState.observe(context as LifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBarReviewOrderUser.visibility = View.VISIBLE
                    binding.rcReviewProduct.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.progressBarReviewOrderUser.visibility = View.GONE

                    Log.d("ReviewUser", "Đã nhận ${state.data.size} đánh giá từ API")
                    val filteredReviews = state.data.filter { it.productId == productId }
                    Log.d("ReviewUser", "Sau khi lọc: ${filteredReviews.size} đánh giá")

                    if (filteredReviews.isEmpty()) {
                        Log.d("ReviewUser", "Không có đánh giá nào cho sản phẩm này")
                    } else {
                        displayReviews(filteredReviews)
                    }
                }
                is UiState.Error -> {
                    binding.progressBarReviewOrderUser.visibility = View.GONE
                    Log.e("ReviewUser", "Lỗi: ${state.message}")
                    Toast.makeText(context, "Không thể tải đánh giá: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayReviews(reviews: List<ProductReview>) {
        // Tạo danh sách để lưu các đánh giá với thông tin người dùng
        val userIds = reviews.map { it.userId }.distinct()
        val userMap = mutableMapOf<String, String>()
        var usersFetched = 0

        // Hiển thị số lượng users cần fetch
        Log.d("ReviewUser", "Cần lấy thông tin cho ${userIds.size} người dùng")

        // Khởi tạo UserService
        val userService = UserService()

        // Trường hợp không có user nào cần fetch
        if (userIds.isEmpty()) {
            // Hiển thị adapter với danh sách rỗng
            val adapter = MyAdapterRecyclerViewOrderReviewProductUser(emptyList())
            binding.rcReviewProduct.adapter = adapter
            binding.rcReviewProduct.layoutManager = LinearLayoutManager(context)
            return
        }

        // Lấy thông tin người dùng cho mỗi userId
        for (userId in userIds) {
            userService.getUserById(userId, object : UserService.UserDataCallBack {
                override fun onSuccess(user: User) {
                    // Lưu tên người dùng vào map
                    userMap[userId] = user.fullName
                    usersFetched++

                    // Log tiến trình
                    Log.d("ReviewUser", "Đã lấy ${usersFetched}/${userIds.size} thông tin người dùng")

                    // Nếu đã lấy đủ thông tin của tất cả người dùng
                    if (usersFetched == userIds.size) {
                        processReviewsWithUserInfo(reviews, userMap)
                    }
                }

                override fun onFailure(errorMessage: String) {
                    // Lưu tên mặc định vào map
                    userMap[userId] = "Người dùng ẩn danh"
                    usersFetched++

                    // Log lỗi
                    Log.e("ReviewUser", "Không thể lấy thông tin người dùng $userId: $errorMessage")

                    // Nếu đã lấy đủ thông tin của tất cả người dùng
                    if (usersFetched == userIds.size) {
                        processReviewsWithUserInfo(reviews, userMap)
                    }
                }
            })
        }
    }

    private fun processReviewsWithUserInfo(reviews: List<ProductReview>, userMap: Map<String, String>) {
        if (reviews.isEmpty()) {
            Log.d("ReviewUser", "Không có đánh giá nào để hiển thị")
            return
        }

        // Chuyển đổi đánh giá thành đối tượng hiển thị
        val reviewItems = reviews.map { review ->
            // Log để debug
            Log.d("ReviewUser", "Đang xử lý review ID: ${review.id}, UserId: ${review.userId}")

            ItemRecyclerViewOrderReviewProductUser(
                id = review.id,
                orderId = review.orderId,
                orderItemId = review.orderItemId,
                userId = review.userId,
                userName = userMap[review.userId] ?: "Người dùng ẩn danh",
                productId = review.productId,
                productName = item.name,
                // Kiểm tra null và trường tên (color hay colorName)
                colorName = review.colorName ?: "",
                size = review.size ?: "",
                rating = review.rating,
                comment = review.comment,
                images = review.images,
                createdAt = review.createdAt
            )
        }

        // Sắp xếp đánh giá mới nhất lên đầu
        val sortedReviews = reviewItems.sortedByDescending { it.createdAt }

        // Log kết quả cuối cùng
        Log.d("ReviewUser", "Hiển thị ${sortedReviews.size} đánh giá")

        // Đảm bảo chúng ta đang ở trong main thread
        binding.root.post {
            try {
                // Hiển thị lên UI
                val adapter = MyAdapterRecyclerViewOrderReviewProductUser(sortedReviews)
                binding.rcReviewProduct.adapter = adapter
                binding.rcReviewProduct.layoutManager = LinearLayoutManager(context)
                // Đảm bảo RecyclerView hiển thị
                binding.rcReviewProduct.visibility = View.VISIBLE
                Log.d("ReviewUser", "Đã cài đặt adapter với ${sortedReviews.size} items")
            } catch (e: Exception) {
                Log.e("ReviewUser", "Lỗi khi cài đặt adapter: ${e.message}", e)
            }
        }
    }
}