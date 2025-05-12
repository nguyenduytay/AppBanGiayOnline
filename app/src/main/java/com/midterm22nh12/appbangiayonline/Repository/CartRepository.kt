package com.midterm22nh12.appbangiayonline.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository xử lý các hoạt động liên quan đến giỏ hàng
 */
class CartRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cartCollection = firestore.collection("carts")
    private val productsCollection = firestore.collection("products")

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng của người dùng
     * @param userId ID của người dùng
     * @return Flow chứa danh sách sản phẩm trong giỏ hàng
     */
    fun getUserCart(userId: String): Flow<List<CartItem>> = flow {
        try {
            // Lấy document giỏ hàng của người dùng
            val cartSnapshot = cartCollection.document(userId).get().await()

            // Nếu không có giỏ hàng, trả về danh sách rỗng
            if (!cartSnapshot.exists()) {
                emit(emptyList())
                return@flow
            }

            // Lấy danh sách tham chiếu đến sản phẩm
            val cartData = cartSnapshot.data ?: mapOf()
            val cartItems = mutableListOf<CartItem>()

            // Duyệt qua từng item trong giỏ hàng
            for ((itemId, value) in cartData) {
                if (value is Map<*, *>) {
                    // ID sản phẩm
                    val productId = value["productId"] as? String ?: continue
                    // Tên màu sắc đã chọn
                    val colorName = value["colorName"] as? String ?: continue
                    // Kích cỡ đã chọn
                    val selectedSize = value["size"] as? String ?: continue
                    // Số lượng
                    val quantity = (value["quantity"] as? Long)?.toInt() ?: 1

                    // Lấy thông tin sản phẩm từ Firestore
                    val productDoc = productsCollection.document(productId).get().await()
                    val product = productDoc.toObject(Product::class.java) ?: continue

                    // Tìm màu đã chọn dựa trên tên
                    val selectedColor = product.colors.find { it.name == colorName } ?: continue

                    // Kiểm tra xem size có tồn tại trong danh sách size của sản phẩm không
                    val selectedSizeObj = product.sizes.find { it.value == selectedSize }

                    // Thêm vào giỏ hàng nếu sản phẩm khả dụng
                    if (selectedSizeObj != null &&
                        selectedSizeObj.status == "available" &&
                        selectedColor.status == "available" &&
                        selectedColor.stock > 0) {
                        val cartItem = CartItem(
                            product = product,
                            selectedColor = selectedColor,
                            selectedSize = selectedSize,
                            quantity = quantity
                        )
                        cartItems.add(cartItem)
                    }
                }
            }

            emit(cartItems)
        } catch (e: Exception) {
            throw Exception("Không thể lấy giỏ hàng: ${e.message}")
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc đã chọn
     * @param size Kích cỡ đã chọn
     * @param quantity Số lượng
     */
    suspend fun addToCart(
        userId: String,
        productId: String,
        colorName: String,
        size: String,
        quantity: Int = 1
    ) {
        try {
            // Kiểm tra tình trạng sản phẩm trước khi thêm vào giỏ
            val isAvailable = checkProductAvailability(productId, colorName, size)
            if (!isAvailable) {
                throw Exception("Sản phẩm không khả dụng hoặc đã hết hàng")
            }

            // Tạo ID duy nhất cho item trong giỏ hàng
            val itemId = "${productId}_${colorName}_$size"

            // Dữ liệu của item
            val cartItemData = hashMapOf(
                "productId" to productId,
                "colorName" to colorName,
                "size" to size,
                "quantity" to quantity,
                "addedAt" to System.currentTimeMillis()
            )

            // Thêm/cập nhật item vào giỏ hàng
            cartCollection.document(userId).set(
                hashMapOf(itemId to cartItemData),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            throw Exception("Không thể thêm vào giỏ hàng: ${e.message}")
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @param quantity Số lượng mới
     */
    suspend fun updateCartItemQuantity(
        userId: String,
        productId: String,
        colorName: String,
        size: String,
        quantity: Int
    ) {
        try {
            if (quantity <= 0) {
                // Nếu số lượng <= 0, xóa sản phẩm khỏi giỏ hàng
                removeFromCart(userId, productId, colorName, size)
                return
            }

            // Kiểm tra tình trạng sản phẩm trước khi cập nhật số lượng
            val isAvailable = checkProductAvailability(productId, colorName, size)
            if (!isAvailable) {
                throw Exception("Sản phẩm không khả dụng hoặc đã hết hàng")
            }

            // Kiểm tra xem số lượng yêu cầu có vượt quá số lượng tồn kho không
            val productDoc = productsCollection.document(productId).get().await()
            val product = productDoc.toObject(Product::class.java) ?: throw Exception("Không tìm thấy sản phẩm")
            val color = product.colors.find { it.name == colorName } ?: throw Exception("Không tìm thấy màu sắc")

            if (quantity > color.stock) {
                throw Exception("Số lượng vượt quá tồn kho (chỉ còn ${color.stock} sản phẩm)")
            }

            // ID của item trong giỏ hàng
            val itemId = "${productId}_${colorName}_$size"

            // Cập nhật số lượng
            cartCollection.document(userId).update(
                "$itemId.quantity", quantity
            ).await()
        } catch (e: Exception) {
            throw Exception("Không thể cập nhật số lượng: ${e.message}")
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     */
    suspend fun removeFromCart(
        userId: String,
        productId: String,
        colorName: String,
        size: String
    ) {
        try {
            // ID của item trong giỏ hàng
            val itemId = "${productId}_${colorName}_$size"

            // Xóa trường dữ liệu
            cartCollection.document(userId).update(
                mapOf(itemId to com.google.firebase.firestore.FieldValue.delete())
            ).await()
        } catch (e: Exception) {
            throw Exception("Không thể xóa khỏi giỏ hàng: ${e.message}")
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng sau khi tạo đơn hàng
     * @param userId ID của người dùng
     */
    suspend fun clearCart(userId: String) {
        try {
            // Xóa document giỏ hàng
            cartCollection.document(userId).delete().await()
        } catch (e: Exception) {
            throw Exception("Không thể xóa giỏ hàng: ${e.message}")
        }
    }

    /**
     * Kiểm tra sản phẩm đã có trong giỏ hàng chưa
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @return Kết quả kiểm tra
     */
    suspend fun isProductInCart(
        userId: String,
        productId: String,
        colorName: String,
        size: String
    ): Boolean {
        try {
            // ID của item trong giỏ hàng
            val itemId = "${productId}_${colorName}_$size"

            // Lấy document giỏ hàng
            val cartSnapshot = cartCollection.document(userId).get().await()

            if (!cartSnapshot.exists()) return false

            val cartData = cartSnapshot.data ?: return false
            return cartData.containsKey(itemId)
        } catch (e: Exception) {
            throw Exception("Không thể kiểm tra sản phẩm: ${e.message}")
        }
    }

    /**
     * Kiểm tra tính khả dụng của sản phẩm trước khi thêm vào giỏ hàng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param sizeValue Giá trị kích cỡ
     * @return Kết quả kiểm tra
     */
    suspend fun checkProductAvailability(
        productId: String,
        colorName: String,
        sizeValue: String
    ): Boolean {
        try {
            // Lấy thông tin sản phẩm
            val productDoc = productsCollection.document(productId).get().await()
            val product = productDoc.toObject(Product::class.java) ?: throw Exception("Không tìm thấy sản phẩm")

            // Tìm màu đã chọn
            val selectedColor = product.colors.find { it.name == colorName }
                ?: return false

            // Kiểm tra trạng thái màu và tồn kho
            if (selectedColor.status != "available" || selectedColor.stock <= 0) {
                return false
            }

            // Tìm size đã chọn
            val selectedSize = product.sizes.find { it.value == sizeValue }
                ?: return false

            // Kiểm tra trạng thái size
            return selectedSize.status == "available"
        } catch (e: Exception) {
            throw Exception("Không thể kiểm tra tình trạng sản phẩm: ${e.message}")
        }
    }

    /**
     * Lấy số lượng tồn kho của sản phẩm với màu và size cụ thể
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @return Số lượng tồn kho
     */
    suspend fun getProductStock(productId: String, colorName: String): Int {
        try {
            // Lấy thông tin sản phẩm
            val productDoc = productsCollection.document(productId).get().await()
            val product = productDoc.toObject(Product::class.java) ?: throw Exception("Không tìm thấy sản phẩm")

            // Tìm màu đã chọn
            val selectedColor = product.colors.find { it.name == colorName }
                ?: throw Exception("Không tìm thấy màu sắc")

            return selectedColor.stock
        } catch (e: Exception) {
            throw Exception("Không thể lấy thông tin tồn kho: ${e.message}")
        }
    }

    /**
     * Đếm số lượng sản phẩm trong giỏ hàng
     * @param userId ID người dùng
     * @return Số lượng sản phẩm
     */
    suspend fun getCartItemCount(userId: String): Int {
        try {
            val cartSnapshot = cartCollection.document(userId).get().await()

            if (!cartSnapshot.exists()) return 0

            val cartData = cartSnapshot.data ?: return 0
            return cartData.size
        } catch (e: Exception) {
            throw Exception("Không thể đếm số lượng sản phẩm trong giỏ hàng: ${e.message}")
        }
    }
}