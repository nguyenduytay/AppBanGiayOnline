package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.databinding.EvaluateUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel


class evaluate_user(
    private val context: Context,
    private val binding: EvaluateUserBinding,
    private val item: ItemRecyclerViewConfirmation
) {
    private lateinit var orderViewModel: OrderViewModel
    private var userId: String = ""

    // Danh sách các sao để đánh giá
    private val listStar: List<android.widget.ImageView> =
        listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

    // Biến để theo dõi số sao người dùng đã chọn
    private var currentRating = 0

    init {
        setUpView()
    }

    private fun setUpView() {
        binding.ivBackEvaluateUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
        orderViewModel = (context as MainActivityUser).getSharedOrderViewModel()
        showProductOrder()
        productReview()
    }

    @SuppressLint("SetTextI18n")
    private fun showProductOrder() {

        binding.btSubmit.setBackgroundColor(Color.parseColor("#FF5722"))

        // Hiển thị thông tin sản phẩm
        binding.tvNameProductOrderUser.text = item.productName
        binding.tvProductVariant.text = "Màu: ${item.colorName}  Size: ${item.size}"

        // Load hình ảnh sản phẩm
        Glide.with(binding.imgProduct.context)
            .load(item.productImage)
            .placeholder(R.drawable.shoes)
            .into(binding.imgProduct)

        // Thiết lập sự kiện click cho từng sao
        listStar.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                // Cập nhật số sao đã chọn
                currentRating = index + 1

                // Cập nhật giao diện sao
                updateStarUI(listStar, currentRating)

                // Kiểm tra điều kiện để kích hoạt nút gửi
                checkSubmitButtonStatus(currentRating)
            }
        }

        // Thêm TextWatcher cho EditText đánh giá để kiểm tra khi người dùng nhập nội dung
        binding.edtRating.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: android.text.Editable?) {
                // Kiểm tra điều kiện để kích hoạt nút gửi
                checkSubmitButtonStatus(currentRating)
            }
        })
    }

    // Hàm cập nhật giao diện sao dựa trên số sao người dùng đã chọn
    private fun updateStarUI(stars: List<android.widget.ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            // Đổi hình sao đầy nếu index < rating, ngược lại dùng hình sao rỗng
            if (index < rating) {
                star.setImageResource(R.mipmap.star_1_foreground)// Thay bằng resource id của hình sao đầy
            } else {
                star.setImageResource(R.drawable.star)     // Thay bằng resource id của hình sao rỗng
            }
        }
    }

    // Hàm kiểm tra điều kiện để kích hoạt nút gửi
    private fun checkSubmitButtonStatus(starRating: Int) {
        // Kiểm tra nếu đã chọn ít nhất 1 sao và đã nhập nội dung đánh giá
        val hasRating = starRating > 0
        val hasComment = binding.edtRating.text.toString().trim().isNotEmpty()

        // Kích hoạt nút gửi chỉ khi đã có đánh giá sao và nội dung
        if (hasRating && hasComment) {
            // binding.btSubmit.isEnabled = true
            binding.btSubmit.setBackgroundColor(Color.parseColor("#FF5722")) // Màu khi nút được kích hoạt
        } else {
            //binding.btSubmit.isEnabled = false
            binding.btSubmit.setBackgroundColor(Color.parseColor("#BB5E5E")) // Màu khi nút bị vô hiệu hóa
        }
    }

    private fun productReview() {
        val firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser?.uid ?: ""

        binding.btSubmit.setOnClickListener {
            val orderId = item.orderId
            val orderItemId = item.orderItemId
            val productId = item.productId
            val rating = currentRating.toFloat()
            val comment = binding.edtRating.text.toString()

            if (currentRating == 0) {
                Toast.makeText(context, "Vui lòng đánh giá sản phẩm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (comment.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            orderId?.let { oId ->
                orderItemId?.let { it1 ->
                    productId?.let { it2 ->
                        // Thêm Observer để theo dõi trạng thái gửi đánh giá
                        item.colorName?.let { it3 ->
                            item.size?.let { it4 ->
                                item.productName?.let { it5 ->
                                    orderViewModel.addProductReview(
                                        orderId = oId,
                                        orderItemId = it1,
                                        userId = userId,
                                        productId = it2,
                                        productName = it5,
                                        colorName = it3,
                                        size = it4,
                                        rating = rating,
                                        comment = comment,
                                        images = emptyList() // Sử dụng emptyList() thay vì null
                                    )
                                }
                            }
                        }

                        // Theo dõi trạng thái gửi đánh giá
                        orderViewModel.addReviewState.observe(context as LifecycleOwner) { state ->
                            when (state) {
                                is UiState.Loading -> {
                                    // Hiển thị loading indicator
                                }

                                is UiState.Success -> {
                                    Toast.makeText(
                                        context,
                                        "Cảm ơn bạn đã đánh giá sản phẩm",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Xóa sản phẩm khi đã đánh giá xong
                                    orderViewModel.cancelOrder(
                                        orderItemId =it1 ,
                                        "evaluate"
                                    )
                                    binding.root.visibility = View.GONE
                                    (context as MainActivityUser).returnToPreviousOverlay()
                                    reset()
                                    // Hủy observer sau khi hoàn thành
                                    orderViewModel.addReviewState.removeObservers(context as LifecycleOwner)
                                }

                                is UiState.Error -> {
                                    Toast.makeText(
                                        context,
                                        "Lỗi: ${state.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //cập nhật lại giao diện
    private fun reset() {
        listStar.forEachIndexed { _, imageView ->
            imageView.setImageResource(R.drawable.star)
        }
        currentRating = 0
        binding.edtRating.text.clear()
    }
}