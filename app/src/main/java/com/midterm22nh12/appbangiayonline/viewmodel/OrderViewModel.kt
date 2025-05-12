package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.Order
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderStatusHistory
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.model.Entity.Order.ProductReview
import com.midterm22nh12.appbangiayonline.repository.CartRepository
import com.midterm22nh12.appbangiayonline.repository.OrderRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel xử lý các logic liên quan đến đơn hàng và giỏ hàng
 */
class OrderViewModel(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    // UI State cho tạo đơn hàng
    private val _createOrderState = MutableLiveData<UiState<String>>()
    val createOrderState: LiveData<UiState<String>> = _createOrderState

    // UI State cho danh sách đơn hàng
    private val _userOrdersState = MutableLiveData<UiState<List<Order>>>()
    val userOrdersState: LiveData<UiState<List<Order>>> = _userOrdersState

    // UI State cho chi tiết đơn hàng
    private val _orderDetailState = MutableLiveData<UiState<OrderWithItems>>()
    val orderDetailState: LiveData<UiState<OrderWithItems>> = _orderDetailState

    // UI State cho lịch sử trạng thái đơn hàng
    private val _orderHistoryState = MutableLiveData<UiState<List<OrderStatusHistory>>>()
    val orderHistoryState: LiveData<UiState<List<OrderStatusHistory>>> = _orderHistoryState

    // UI State cho đánh giá sản phẩm
    private val _productReviewsState = MutableLiveData<UiState<List<ProductReview>>>()
    val productReviewsState: LiveData<UiState<List<ProductReview>>> = _productReviewsState

    // UI State cho việc thêm đánh giá
    private val _addReviewState = MutableLiveData<UiState<String>>()
    val addReviewState: LiveData<UiState<String>> = _addReviewState

    // UI State cho danh sách đơn hàng theo trạng thái (Admin)
    private val _ordersByStatusState = MutableLiveData<UiState<List<Order>>>()
    val ordersByStatusState: LiveData<UiState<List<Order>>> = _ordersByStatusState

    /**
     * Tạo đơn hàng mới từ giỏ hàng
     * @param userId ID người dùng
     * @param shippingAddress Địa chỉ giao hàng
     * @param phoneNumber Số điện thoại
     * @param paymentMethod Phương thức thanh toán
     * @param note Ghi chú
     * @param cartItems Danh sách sản phẩm trong giỏ hàng
     */
    fun createOrderFromCart(
        userId: String,
        shippingAddress: String,
        phoneNumber: String,
        paymentMethod: String,
        note: String = "",
        cartItems: List<CartItem>
    ) {
        viewModelScope.launch {
            _createOrderState.value = UiState.Loading
            try {
                // Kiểm tra lại tình trạng sản phẩm trước khi tạo đơn hàng
                for (cartItem in cartItems) {
                    val isAvailable = cartRepository.checkProductAvailability(
                        cartItem.product.id,
                        cartItem.selectedColor.name,
                        cartItem.selectedSize
                    )

                    if (!isAvailable) {
                        _createOrderState.value = UiState.Error(
                            "Sản phẩm ${cartItem.product.name} - ${cartItem.selectedColor.name} - ${cartItem.selectedSize} đã hết hàng hoặc không khả dụng"
                        )
                        return@launch
                    }

                    // Kiểm tra xem số lượng yêu cầu có vượt quá số lượng tồn kho không
                    if (cartItem.quantity > cartItem.selectedColor.stock) {
                        _createOrderState.value = UiState.Error(
                            "Sản phẩm ${cartItem.product.name} - ${cartItem.selectedColor.name} chỉ còn ${cartItem.selectedColor.stock} trong kho"
                        )
                        return@launch
                    }
                }

                // Chuyển đổi CartItem thành OrderItem
                val orderItems = cartItems.map { cartItem ->
                    OrderItem(
                        id = UUID.randomUUID().toString(),
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        productImage = cartItem.selectedColor.image, // Sử dụng hình ảnh của màu sắc
                        price = cartItem.product.price, // Sử dụng giá cơ bản của sản phẩm
                        quantity = cartItem.quantity,
                        size = cartItem.selectedSize,
                        color = cartItem.selectedColor.name,
                        colorImage = cartItem.selectedColor.image,
                        productCode = cartItem.selectedColor.productCode
                    )
                }

                // Tính tổng tiền
                val totalAmount = orderItems.sumOf { it.price * it.quantity }

                // Phí vận chuyển (có thể tính dựa trên logic nghiệp vụ)
                val shippingFee = calculateShippingFee(totalAmount)

                // Tạo đối tượng Order
                val order = Order(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    totalAmount = totalAmount,
                    shippingAddress = shippingAddress,
                    phoneNumber = phoneNumber,
                    paymentMethod = paymentMethod,
                    status = "pending",
                    note = note,
                    shippingFee = shippingFee,
                    paymentStatus = if (paymentMethod == "COD") "unpaid" else "processing"
                )

                // Lưu đơn hàng vào database
                val orderId = orderRepository.createOrder(order, orderItems)

                // Xóa giỏ hàng sau khi đặt hàng thành công
                cartRepository.clearCart(userId)

                _createOrderState.value = UiState.Success(orderId)
            } catch (e: Exception) {
                _createOrderState.value = UiState.Error(e.message ?: "Không thể tạo đơn hàng")
            }
        }
    }

    /**
     * Tính phí vận chuyển dựa trên tổng tiền đơn hàng
     * @param totalAmount Tổng tiền đơn hàng
     * @return Phí vận chuyển
     */
    private fun calculateShippingFee(totalAmount: Long): Long {
        // Logic tính phí vận chuyển (ví dụ)
        return when {
            totalAmount >= 1000000 -> 0L // Miễn phí vận chuyển cho đơn từ 1 triệu
            totalAmount >= 500000 -> 15000L // Giảm phí cho đơn từ 500k
            else -> 30000L // Phí mặc định
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     * @param userId ID người dùng
     */
    fun getUserOrders(userId: String) {
        viewModelScope.launch {
            _userOrdersState.value = UiState.Loading
            try {
                orderRepository.getUserOrders(userId).collect { orders ->
                    _userOrdersState.value = UiState.Success(orders)
                }
            } catch (e: Exception) {
                _userOrdersState.value = UiState.Error(e.message ?: "Không thể lấy danh sách đơn hàng")
            }
        }
    }

    /**
     * Lấy chi tiết đơn hàng
     * @param orderId ID đơn hàng
     */
    fun getOrderDetail(orderId: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading
            try {
                val orderWithItems = orderRepository.getOrderWithItems(orderId)
                _orderDetailState.value = UiState.Success(orderWithItems)
            } catch (e: Exception) {
                _orderDetailState.value = UiState.Error(e.message ?: "Không thể lấy chi tiết đơn hàng")
            }
        }
    }

    /**
     * Lấy lịch sử trạng thái đơn hàng
     * @param orderId ID đơn hàng
     */
    fun getOrderStatusHistory(orderId: String) {
        viewModelScope.launch {
            _orderHistoryState.value = UiState.Loading
            try {
                orderRepository.getOrderStatusHistory(orderId).collect { history ->
                    _orderHistoryState.value = UiState.Success(history)
                }
            } catch (e: Exception) {
                _orderHistoryState.value = UiState.Error(e.message ?: "Không thể lấy lịch sử đơn hàng")
            }
        }
    }

    /**
     * Hủy đơn hàng
     * @param orderId ID đơn hàng
     * @param reason Lý do hủy
     * @param userId ID người dùng thực hiện hủy
     */
    fun cancelOrder(orderId: String, reason: String, userId: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading
            try {
                orderRepository.cancelOrder(orderId, reason, userId)
                // Cập nhật lại thông tin đơn hàng sau khi hủy
                getOrderDetail(orderId)
            } catch (e: Exception) {
                _orderDetailState.value = UiState.Error(e.message ?: "Không thể hủy đơn hàng")
            }
        }
    }

    /**
     * Gửi đánh giá sản phẩm
     * @param orderId ID đơn hàng
     * @param orderItemId ID sản phẩm trong đơn hàng
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param rating Số sao đánh giá
     * @param comment Nội dung đánh giá
     * @param images Danh sách ảnh đính kèm
     */
    fun addProductReview(
        orderId: String,
        orderItemId: String,
        userId: String,
        productId: String,
        rating: Float,
        comment: String,
        images: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _addReviewState.value = UiState.Loading
            try {
                val review = ProductReview(
                    orderId = orderId,
                    orderItemId = orderItemId,
                    userId = userId,
                    productId = productId,
                    rating = rating,
                    comment = comment,
                    images = images
                )

                val reviewId = orderRepository.addProductReview(review)
                _addReviewState.value = UiState.Success(reviewId)

                // Cập nhật lại danh sách đánh giá
                getProductReviews(productId)
            } catch (e: Exception) {
                _addReviewState.value = UiState.Error(e.message ?: "Không thể gửi đánh giá")
            }
        }
    }

    /**
     * Lấy danh sách đánh giá sản phẩm
     * @param productId ID sản phẩm
     */
    fun getProductReviews(productId: String) {
        viewModelScope.launch {
            _productReviewsState.value = UiState.Loading
            try {
                orderRepository.getProductReviews(productId).collect { reviews ->
                    _productReviewsState.value = UiState.Success(reviews)
                }
            } catch (e: Exception) {
                _productReviewsState.value = UiState.Error(e.message ?: "Không thể lấy đánh giá sản phẩm")
            }
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng (dành cho Admin)
     * @param orderId ID đơn hàng
     * @param newStatus Trạng thái mới
     * @param note Ghi chú
     * @param adminId ID của admin thực hiện cập nhật
     */
    fun updateOrderStatus(orderId: String, newStatus: String, note: String = "", adminId: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading
            try {
                orderRepository.updateOrderStatus(orderId, newStatus, note, adminId)
                // Cập nhật lại thông tin đơn hàng
                getOrderDetail(orderId)
            } catch (e: Exception) {
                _orderDetailState.value = UiState.Error(e.message ?: "Không thể cập nhật trạng thái")
            }
        }
    }

    /**
     * Cập nhật trạng thái thanh toán (dành cho Admin hoặc hệ thống thanh toán)
     * @param orderId ID đơn hàng
     * @param paymentStatus Trạng thái thanh toán mới
     */
    fun updatePaymentStatus(orderId: String, paymentStatus: String) {
        viewModelScope.launch {
            try {
                orderRepository.updatePaymentStatus(orderId, paymentStatus)
                // Cập nhật lại thông tin đơn hàng
                getOrderDetail(orderId)
            } catch (e: Exception) {
                _orderDetailState.value = UiState.Error(e.message ?: "Không thể cập nhật trạng thái thanh toán")
            }
        }
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái (dành cho Admin)
     * @param status Trạng thái cần lọc
     */
    fun getOrdersByStatus(status: String) {
        viewModelScope.launch {
            _ordersByStatusState.value = UiState.Loading
            try {
                orderRepository.getOrdersByStatus(status).collect { orders ->
                    _ordersByStatusState.value = UiState.Success(orders)
                }
            } catch (e: Exception) {
                _ordersByStatusState.value = UiState.Error(e.message ?: "Không thể lấy danh sách đơn hàng theo trạng thái")
            }
        }
    }

    /**
     * Lấy danh sách đơn hàng theo khoảng thời gian (dành cho Admin)
     * @param startTime Thời điểm bắt đầu (timestamp)
     * @param endTime Thời điểm kết thúc (timestamp)
     */
    fun getOrdersByTimeRange(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            _ordersByStatusState.value = UiState.Loading
            try {
                orderRepository.getOrdersByTimeRange(startTime, endTime).collect { orders ->
                    _ordersByStatusState.value = UiState.Success(orders)
                }
            } catch (e: Exception) {
                _ordersByStatusState.value = UiState.Error(e.message ?: "Không thể lấy danh sách đơn hàng theo khoảng thời gian")
            }
        }
    }
}