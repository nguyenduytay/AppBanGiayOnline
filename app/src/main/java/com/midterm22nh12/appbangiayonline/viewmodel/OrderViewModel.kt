package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.midterm22nh12.appbangiayonline.Repository.OrderRepository
import com.midterm22nh12.appbangiayonline.Utils.Event
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.Order
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor

class OrderViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val TAG = "OrderViewModel"

    // LiveData cho giỏ hàng
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    // LiveData cho tổng giá trị giỏ hàng
    private val _cartTotal = MutableLiveData<Long>(0)
    val cartTotal: LiveData<Long> = _cartTotal

    // LiveData cho kết quả tạo đơn hàng
    private val _createOrderResult = MutableLiveData<Event<Result<Order>>>()
    val createOrderResult: LiveData<Event<Result<Order>>> = _createOrderResult

    // LiveData cho danh sách đơn hàng
    private val _userOrders = MutableLiveData<Event<Result<List<Order>>>>()
    val userOrders: LiveData<Event<Result<List<Order>>>> = _userOrders

    // LiveData cho chi tiết đơn hàng
    private val _orderDetails = MutableLiveData<Event<Result<OrderWithItems>>>()
    val orderDetails: LiveData<Event<Result<OrderWithItems>>> = _orderDetails

    // LiveData cho kết quả hủy đơn hàng
    private val _cancelOrderResult = MutableLiveData<Event<Result<Unit>>>()
    val cancelOrderResult: LiveData<Event<Result<Unit>>> = _cancelOrderResult

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    fun addToCart(product: Product, selectedColor: ProductColor, selectedSize: String, quantity: Int = 1) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()

        // Kiểm tra xem sản phẩm này đã có trong giỏ hàng chưa (cùng ID, màu, size)
        val existingItemIndex = currentCart.indexOfFirst {
            it.product.id == product.id &&
                    it.selectedColor.name == selectedColor.name &&
                    it.selectedSize == selectedSize
        }

        if (existingItemIndex != -1) {
            // Cập nhật số lượng nếu sản phẩm đã tồn tại
            val existingItem = currentCart[existingItemIndex]
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
            currentCart[existingItemIndex] = updatedItem
        } else {
            // Thêm sản phẩm mới vào giỏ hàng
            currentCart.add(CartItem(product, selectedColor, selectedSize, quantity))
        }

        // Cập nhật giỏ hàng
        _cartItems.value = currentCart

        // Tính lại tổng giá trị
        calculateCartTotal()
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    fun removeFromCart(position: Int) {
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        if (position >= 0 && position < currentCart.size) {
            currentCart.removeAt(position)
            _cartItems.value = currentCart
            calculateCartTotal()
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    fun updateCartItemQuantity(position: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(position)
            return
        }

        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        if (position >= 0 && position < currentCart.size) {
            val item = currentCart[position]
            val updatedItem = item.copy(quantity = newQuantity)
            currentCart[position] = updatedItem
            _cartItems.value = currentCart
            calculateCartTotal()
        }
    }

    /**
     * Tính tổng giá trị giỏ hàng
     */
    private fun calculateCartTotal() {
        val currentCart = _cartItems.value ?: emptyList()
        val total = currentCart.sumOf { item -> item.product.price * item.quantity }
        _cartTotal.value = total
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    fun clearCart() {
        _cartItems.value = emptyList()
        _cartTotal.value = 0
    }

    /**
     * Tạo đơn hàng mới
     */
    fun createOrder(shippingAddress: String, phoneNumber: String, paymentMethod: String, note: String = "") {
        _isLoading.value = true

        val cartItems = _cartItems.value ?: emptyList()
        if (cartItems.isEmpty()) {
            _createOrderResult.value = Event(Result.failure(Exception("Giỏ hàng trống")))
            _isLoading.value = false
            return
        }

        // Chuyển đổi từ CartItem sang OrderItem
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                productImage = cartItem.selectedColor.image,
                price = cartItem.product.price,
                quantity = cartItem.quantity,
                size = cartItem.selectedSize,
                color = cartItem.selectedColor.name,
                colorImage = cartItem.selectedColor.image,
                productCode = cartItem.selectedColor.productCode
            )
        }

        // Gọi repository để tạo đơn hàng
        orderRepository.createOrder(
            items = orderItems,
            shippingAddress = shippingAddress,
            phoneNumber = phoneNumber,
            paymentMethod = paymentMethod,
            note = note
        ) { result ->
            _isLoading.value = false
            _createOrderResult.value = Event(result)

            // Nếu tạo đơn hàng thành công, xóa giỏ hàng
            if (result.isSuccess) {
                clearCart()
            }
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     */
    fun getUserOrders() {
        _isLoading.value = true
        orderRepository.getUserOrders { result ->
            _isLoading.value = false
            _userOrders.value = Event(result)
        }
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    fun getOrderDetails(orderId: String) {
        _isLoading.value = true
        orderRepository.getOrderDetails(orderId) { result ->
            _isLoading.value = false
            _orderDetails.value = Event(result)
        }
    }

    /**
     * Hủy đơn hàng
     */
    fun cancelOrder(orderId: String, reason: String) {
        _isLoading.value = true
        orderRepository.cancelOrder(orderId, reason) { result ->
            _isLoading.value = false
            _cancelOrderResult.value = Event(result)

            // Nếu hủy đơn hàng thành công, cập nhật lại danh sách đơn hàng
            if (result.isSuccess) {
                getUserOrders()
            }
        }
    }
}
