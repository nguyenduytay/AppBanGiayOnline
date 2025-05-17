package com.midterm22nh12.appbangiayonline.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
import com.midterm22nh12.appbangiayonline.Repository.CartRepository
import com.midterm22nh12.appbangiayonline.Repository.OrderRepository
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel xử lý các logic liên quan đến đơn hàng và giỏ hàng
 */
class OrderViewModel(application: Application): AndroidViewModel(application) {
    private val orderRepository = OrderRepository()
    private val cartRepository = CartRepository()
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

    // LiveData để theo dõi danh sách sản phẩm chờ xác nhận
    private val _pendingOrderItemsState = MutableLiveData<UiState<List<ItemRecyclerViewConfirmation>>>()
    val pendingOrderItemsState: LiveData<UiState<List<ItemRecyclerViewConfirmation>>> = _pendingOrderItemsState

    // UI State cho tất cả đơn hàng kèm OrderItems (cho trang Admin)
    private val _allOrdersWithItemsState = MutableLiveData<UiState<List<OrderWithItems>>>()
    val allOrdersWithItemsState: LiveData<UiState<List<OrderWithItems>>> = _allOrdersWithItemsState

    // UI State cho tất cả đơn hàng kèm OrderItems (cho trang Admin)
    private val _allOrdersWithItemsStateRevenue = MutableLiveData<UiState<List<OrderWithItems>>>()
    val allOrdersWithItemsStateRevenue: LiveData<UiState<List<OrderWithItems>>> = _allOrdersWithItemsStateRevenue


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
                        productCode = cartItem.selectedColor.productCode,
                        status = "pending" // Đặt trạng thái đơn hàng là "pending"
                    )
                }

                // Tính tổng tiền
                var totalAmount = orderItems.sumOf { it.price * it.quantity }

                // Phí vận chuyển (có thể tính dựa trên logic nghiệp vụ)
                val shippingFee = calculateShippingFee(totalAmount)

                if(shippingFee != 0L){
                    totalAmount += shippingFee
                }
                // Tạo đối tượng Order
                val order = Order(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    totalAmount = totalAmount,
                    shippingAddress = shippingAddress,
                    phoneNumber = phoneNumber,
                    paymentMethod = paymentMethod,
                    note = note,
                    shippingFee = shippingFee,
                    paymentStatus = if (paymentMethod == "COD") "unpaid" else "processing"
                )

                // Lưu đơn hàng vào database
                val orderId = orderRepository.createOrder(order, orderItems)

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
     * Cập nhật lại trạng thái đơn hàng
     * @param orderId ID đơn hàng
     * @param reason Lý do hủy
     * @param userId ID người dùng thực hiện hủy
     */
    fun cancelOrder(orderItemId: String, note: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading
            try {
                orderRepository.updateOrderStatus(orderItemId, note)
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
        productName: String,
        colorName: String,
        size: String,
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
                    productName = productName,
                    colorName = colorName,
                    size = size,
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
    fun updateOrderStatus(orderId : String, orderItemId: String, newStatus: String) {
        viewModelScope.launch {
            _orderDetailState.value = UiState.Loading
            try {
                orderRepository.updateOrderStatus(orderItemId, newStatus)
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

    // Theo dõi đơn hàng realtime
    fun observePendingOrderItems(userId: String) {
        viewModelScope.launch {
            try {
                // Bắt đầu loading
                _pendingOrderItemsState.value = UiState.Loading

                // Lắng nghe thay đổi của đơn hàng
                orderRepository.getPendingOrderItemsRealtime(userId)
                    .collect { pendingProducts ->
                        // Log để debug
                        Log.d("OrderViewModel", "Nhận được ${pendingProducts.size} items")

                        // Cập nhật state
                        _pendingOrderItemsState.value = UiState.Success(pendingProducts)
                    }
            } catch (e: Exception) {
                // Xử lý lỗi
                Log.e("OrderViewModel", "Lỗi: ${e.message}", e)
                _pendingOrderItemsState.value = UiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }
    //hủy đơn hàng xóa đơn hàng
    fun cancelOrderItemByProductId(
        productId: String,
        orderId: String,
        colorName: String,
        index : Int
    ) {
        viewModelScope.launch {
            try {
                // Gọi phương thức hủy item từ repository
                orderRepository.cancelOrderItemByProductId(
                    productId = productId,
                    orderId = orderId,
                    colorName,
                    index
                )

            } catch (e: Exception) {
                // Xử lý lỗi
                Log.e("OrderViewModel", "Lỗi khi hủy item đơn hàng: ${e.message}")
                _userOrdersState.value = UiState.Error(e.message ?: "Không thể hủy item đơn hàng")
            }
        }
    }
    /**
     * Lấy tất cả đơn hàng kèm OrderItems
     */
    fun getAllOrdersWithItems() {
        viewModelScope.launch {
            _allOrdersWithItemsState.value = UiState.Loading
            try {
                val ordersWithItems = orderRepository.getAllOrdersWithItems()
                _allOrdersWithItemsState.value = UiState.Success(ordersWithItems)
            } catch (e: Exception) {
                _allOrdersWithItemsState.value = UiState.Error(e.message ?: "Không thể lấy danh sách đơn hàng")
            }
        }
    }
    /**
     * Lấy tất cả đơn hàng kèm OrderItems
     */
    fun getAllOrdersWithItemsRevenue() {
        viewModelScope.launch {
            _allOrdersWithItemsStateRevenue.value = UiState.Loading
            try {
                val ordersWithItems = orderRepository.getAllOrdersWithItemsRevenue()
                _allOrdersWithItemsStateRevenue.value = UiState.Success(ordersWithItems)
            } catch (e: Exception) {
                _allOrdersWithItemsStateRevenue.value = UiState.Error(e.message ?: "Không thể lấy danh sách đơn hàng")
            }
        }
    }

}