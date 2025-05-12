package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.repository.CartRepository
import kotlinx.coroutines.launch

/**
 * ViewModel xử lý các logic liên quan đến giỏ hàng
 */
class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    // UI State cho giỏ hàng
    private val _cartItemsState = MutableLiveData<UiState<List<CartItem>>>()
    val cartItemsState: LiveData<UiState<List<CartItem>>> = _cartItemsState

    // UI State cho tổng tiền giỏ hàng
    private val _cartTotalState = MutableLiveData<Long>()
    val cartTotalState: LiveData<Long> = _cartTotalState

    // UI State cho số lượng sản phẩm trong giỏ hàng
    private val _cartCountState = MutableLiveData<Int>()
    val cartCountState: LiveData<Int> = _cartCountState

    // UI State cho kiểm tra tình trạng sản phẩm
    private val _availabilityState = MutableLiveData<UiState<Boolean>>()
    val availabilityState: LiveData<UiState<Boolean>> = _availabilityState

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng
     * @param userId ID người dùng
     */
    fun getCartItems(userId: String) {
        viewModelScope.launch {
            _cartItemsState.value = UiState.Loading
            try {
                cartRepository.getUserCart(userId).collect { cartItems ->
                    _cartItemsState.value = UiState.Success(cartItems)

                    // Tính toán tổng tiền
                    calculateCartTotal(cartItems)

                    // Cập nhật số lượng sản phẩm
                    _cartCountState.value = cartItems.size
                }
            } catch (e: Exception) {
                _cartItemsState.value = UiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Tính tổng tiền giỏ hàng
     * @param cartItems Danh sách sản phẩm trong giỏ hàng
     */
    private fun calculateCartTotal(cartItems: List<CartItem>) {
        val total = cartItems.sumOf { item ->
            // Tính tiền cho từng item: giá sản phẩm x số lượng
            item.product.price * item.quantity
        }
        _cartTotalState.value = total
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @param quantity Số lượng
     */
    fun addToCart(userId: String, productId: String, colorName: String, size: String, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                // Kiểm tra tình trạng sản phẩm trước khi thêm vào giỏ hàng
                val isAvailable = cartRepository.checkProductAvailability(productId, colorName, size)

                if (!isAvailable) {
                    _cartItemsState.value = UiState.Error("Sản phẩm đã hết hàng hoặc không khả dụng")
                    return@launch
                }

                // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
                val isInCart = cartRepository.isProductInCart(userId, productId, colorName, size)

                if (isInCart) {
                    // Nếu đã có, cập nhật số lượng thay vì thêm mới
                    updateCartItemQuantity(userId, productId, colorName, size, quantity)
                } else {
                    // Nếu chưa có, thêm mới vào giỏ hàng
                    cartRepository.addToCart(userId, productId, colorName, size, quantity)
                    // Cập nhật giỏ hàng sau khi thêm
                    getCartItems(userId)
                }
            } catch (e: Exception) {
                _cartItemsState.value = UiState.Error(e.message ?: "Không thể thêm vào giỏ hàng")
            }
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @param quantity Số lượng mới (nếu là 0, sẽ xóa sản phẩm khỏi giỏ hàng)
     */
    fun updateCartItemQuantity(
        userId: String,
        productId: String,
        colorName: String,
        size: String,
        quantity: Int
    ) {
        viewModelScope.launch {
            try {
                if (quantity > 0) {
                    // Kiểm tra tình trạng sản phẩm và số lượng có thể cập nhật
                    val isAvailable = cartRepository.checkProductAvailability(productId, colorName, size)

                    if (!isAvailable) {
                        _cartItemsState.value = UiState.Error("Sản phẩm đã hết hàng hoặc không khả dụng")
                        return@launch
                    }
                }

                cartRepository.updateCartItemQuantity(userId, productId, colorName, size, quantity)
                // Cập nhật giỏ hàng sau khi thay đổi số lượng
                getCartItems(userId)
            } catch (e: Exception) {
                _cartItemsState.value = UiState.Error(e.message ?: "Không thể cập nhật số lượng")
            }
        }
    }

    /**
     * Thay đổi số lượng sản phẩm trong giỏ hàng (tăng/giảm)
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @param increment true: tăng lên 1, false: giảm đi 1
     */
    fun changeQuantity(
        userId: String,
        productId: String,
        colorName: String,
        size: String,
        increment: Boolean
    ) {
        viewModelScope.launch {
            try {
                // Lấy danh sách hiện tại
                val currentState = _cartItemsState.value
                if (currentState is UiState.Success) {
                    // Tìm sản phẩm trong giỏ hàng
                    val cartItem = currentState.data.find {
                        it.product.id == productId &&
                                it.selectedColor.name == colorName &&
                                it.selectedSize == size
                    }

                    // Nếu tìm thấy sản phẩm
                    cartItem?.let {
                        val newQuantity = if (increment) it.quantity + 1 else it.quantity - 1

                        // Kiểm tra tình trạng nếu tăng số lượng
                        if (increment && newQuantity > 1) {
                            val isAvailable = cartRepository.checkProductAvailability(productId, colorName, size)

                            if (!isAvailable) {
                                _cartItemsState.value = UiState.Error("Không thể tăng số lượng do sản phẩm đã hết hàng hoặc không khả dụng")
                                return@launch
                            }

                            // Kiểm tra xem số lượng yêu cầu có vượt quá số lượng tồn kho không
                            if (newQuantity > cartItem.selectedColor.stock) {
                                _cartItemsState.value = UiState.Error(
                                    "Không thể tăng số lượng vì chỉ còn ${cartItem.selectedColor.stock} sản phẩm trong kho"
                                )
                                return@launch
                            }
                        }

                        // Cập nhật số lượng, nếu giảm xuống 0 sẽ tự động xóa khỏi giỏ hàng
                        updateCartItemQuantity(userId, productId, colorName, size, newQuantity)
                    }
                }
            } catch (e: Exception) {
                _cartItemsState.value = UiState.Error(e.message ?: "Không thể thay đổi số lượng")
            }
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     */
    fun removeFromCart(userId: String, productId: String, colorName: String, size: String) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(userId, productId, colorName, size)
                // Cập nhật giỏ hàng sau khi xóa
                getCartItems(userId)
            } catch (e: Exception) {
                _cartItemsState.value = UiState.Error(e.message ?: "Không thể xóa khỏi giỏ hàng")
            }
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     * @param userId ID người dùng
     */
    fun clearCart(userId: String) {
        viewModelScope.launch {
            try {
                cartRepository.clearCart(userId)
                // Cập nhật giỏ hàng sau khi xóa
                _cartItemsState.value = UiState.Success(emptyList())
                _cartTotalState.value = 0
                _cartCountState.value = 0
            } catch (e: Exception) {
                _cartItemsState.value = UiState.Error(e.message ?: "Không thể xóa giỏ hàng")
            }
        }
    }

    /**
     * Kiểm tra sản phẩm đã có trong giỏ hàng chưa
     * @param userId ID người dùng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @return LiveData chứa kết quả kiểm tra
     */
    fun checkProductInCart(userId: String, productId: String, colorName: String, size: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        viewModelScope.launch {
            try {
                val isInCart = cartRepository.isProductInCart(userId, productId, colorName, size)
                result.value = isInCart
            } catch (e: Exception) {
                result.value = false
            }
        }

        return result
    }

    /**
     * Kiểm tra tình trạng khả dụng của sản phẩm
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     */
    fun checkProductAvailability(productId: String, colorName: String, size: String) {
        viewModelScope.launch {
            _availabilityState.value = UiState.Loading
            try {
                val isAvailable = cartRepository.checkProductAvailability(productId, colorName, size)
                _availabilityState.value = UiState.Success(isAvailable)
            } catch (e: Exception) {
                _availabilityState.value = UiState.Error(e.message ?: "Không thể kiểm tra tình trạng sản phẩm")
            }
        }
    }
}