package com.midterm22nh12.appbangiayonline.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.Repository.CartRepository
import kotlinx.coroutines.launch

/**
 * ViewModel xử lý các logic liên quan đến giỏ hàng
 */
class CartViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val cartRepository = CartRepository()
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
                Log.d("CartViewModel", "Bắt đầu lấy giỏ hàng")

                // Sử dụng phương thức đồng bộ cải tiến, tránh vấn đề bất đồng bộ
                cartRepository.getUserCart(userId).collect { cartItems ->
                    Log.d("CartViewModel", "Nhận được ${cartItems.size} sản phẩm từ repository")

                    // Debug log để kiểm tra từng sản phẩm
                    cartItems.forEachIndexed { index, item ->
                        Log.d("CartViewModel", "Item $index: ${item.product.name}, color: ${item.selectedColor.name}, size: ${item.selectedSize}")
                    }

                    _cartItemsState.value = UiState.Success(cartItems)

                    // Tính toán tổng tiền
                    calculateCartTotal(cartItems)

                    // Cập nhật số lượng sản phẩm
                    _cartCountState.value = cartItems.size
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi lấy giỏ hàng: ${e.message}")
                _cartItemsState.value = UiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng realtime
     * @param userId ID người dùng
     */
    fun getCartItemsRealtime(userId: String) {
        viewModelScope.launch {
            _cartItemsState.value = UiState.Loading
            try {
                cartRepository.getUserCartRealtime(userId).collect { cartItems ->
                    Log.d("CartViewModel", "Nhận được ${cartItems.size} sản phẩm từ repository (realtime)")

                    _cartItemsState.value = UiState.Success(cartItems)

                    // Tính toán tổng tiền
                    calculateCartTotal(cartItems)

                    // Cập nhật số lượng sản phẩm
                    _cartCountState.value = cartItems.size
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi lấy giỏ hàng realtime: ${e.message}")
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
        Log.d("CartViewModel", "Tổng tiền giỏ hàng: $total")
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
                Log.d("CartViewModel", "Thêm vào giỏ hàng: productId=$productId, colorName=$colorName, size=$size")

                // Kiểm tra tình trạng sản phẩm trước khi thêm vào giỏ hàng
                val isAvailable = cartRepository.checkProductAvailability(productId, colorName, size)

                if (!isAvailable) {
                    _cartItemsState.value = UiState.Error("Sản phẩm đã hết hàng hoặc không khả dụng")
                    return@launch
                }

                // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
                val isInCart = cartRepository.isProductInCart(userId, productId, colorName, size)

                if (isInCart) {
                    Log.d("CartViewModel", "Sản phẩm đã có trong giỏ hàng, cập nhật số lượng")
                    // Nếu đã có, cập nhật số lượng thay vì thêm mới
                    updateCartItemQuantity(userId, productId, colorName, size, quantity)
                } else {
                    Log.d("CartViewModel", "Thêm sản phẩm mới vào giỏ hàng")
                    // Nếu chưa có, thêm mới vào giỏ hàng
                    cartRepository.addToCart(userId, productId, colorName, size, quantity)
                    // Cập nhật giỏ hàng sau khi thêm
                    getCartItems(userId)
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi thêm vào giỏ hàng: ${e.message}")
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
                Log.d("CartViewModel", "Cập nhật số lượng: productId=$productId, colorName=$colorName, size=$size, quantity=$quantity")

                if (quantity > 0) {
                    // Kiểm tra tình trạng sản phẩm và số lượng có thể cập nhật
                    val isAvailable = cartRepository.checkProductAvailability(productId, colorName, size)

                    if (!isAvailable) {
                        _cartItemsState.value = UiState.Error("Sản phẩm đã hết hàng hoặc không khả dụng")
                        return@launch
                    }
                }

                cartRepository.updateCartItemQuantity(userId, productId, colorName, size, quantity)
                Log.d("CartViewModel", "Đã cập nhật số lượng, đang lấy lại giỏ hàng")

                // Cập nhật giỏ hàng sau khi thay đổi số lượng
                getCartItems(userId)
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi cập nhật số lượng: ${e.message}")
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
                        Log.d("CartViewModel", "Thay đổi số lượng từ ${it.quantity} thành $newQuantity")

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
                Log.e("CartViewModel", "Lỗi khi thay đổi số lượng: ${e.message}")
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
                Log.d("CartViewModel-delete", "Xóa khỏi giỏ hàng: productId=$productId, colorName=$colorName, size=$size")

                cartRepository.removeFromCart(userId, productId, colorName, size)

                // Cập nhật giỏ hàng sau khi xóa
                getCartItems(userId)
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi xóa khỏi giỏ hàng: ${e.message}")
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
                Log.d("CartViewModel", "Xóa toàn bộ giỏ hàng")

                cartRepository.clearCart(userId)

                // Cập nhật giỏ hàng sau khi xóa
                _cartItemsState.value = UiState.Success(emptyList())
                _cartTotalState.value = 0
                _cartCountState.value = 0
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi xóa giỏ hàng: ${e.message}")
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
                Log.d("CartViewModel", "Kiểm tra sản phẩm trong giỏ hàng")

                val isInCart = cartRepository.isProductInCart(userId, productId, colorName, size)
                result.value = isInCart
            } catch (e: Exception) {
                Log.e("CartViewModel", "Lỗi khi kiểm tra sản phẩm trong giỏ hàng: ${e.message}")
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