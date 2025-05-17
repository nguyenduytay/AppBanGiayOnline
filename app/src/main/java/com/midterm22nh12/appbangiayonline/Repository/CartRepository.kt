package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * Repository xử lý các hoạt động liên quan đến giỏ hàng sử dụng Realtime Database
 * Cung cấp các phương thức để quản lý giỏ hàng: thêm, sửa, xóa, lấy danh sách sản phẩm
 */
class CartRepository {
    // Khởi tạo kết nối với Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()
    private val cartRef = database.getReference("carts")       // Tham chiếu đến node lưu giỏ hàng
    private val productsRef = database.getReference("products") // Tham chiếu đến node lưu sản phẩm

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng của người dùng
     * Sử dụng Flow để trả về dữ liệu bất đồng bộ
     *
     * Quy trình:
     * 1. Lấy dữ liệu giỏ hàng từ database
     * 2. Duyệt qua từng item trong giỏ hàng
     * 3. Lấy thông tin chi tiết sản phẩm
     * 4. Kiểm tra tính khả dụng của sản phẩm (trạng thái, tồn kho)
     * 5. Tạo và trả về danh sách CartItem
     *
     * @param userId ID của người dùng cần lấy giỏ hàng
     * @return Flow<List<CartItem>> - luồng dữ liệu chứa danh sách sản phẩm trong giỏ hàng
     */
    fun getUserCart(userId: String): Flow<List<CartItem>> = flow {
        try {
            Log.d("CartRepository", "Bắt đầu lấy giỏ hàng cho userId: $userId")

            // Lấy dữ liệu giỏ hàng từ Realtime Database
            val cartSnapshot = cartRef.child(userId).get().await()

            Log.d("CartRepository", "Đã nhận snapshot, tồn tại: ${cartSnapshot.exists()}, childrenCount: ${cartSnapshot.childrenCount}")

            if (!cartSnapshot.exists()) {
                Log.d("CartRepository", "Giỏ hàng trống cho userId: $userId")
                emit(emptyList())
                return@flow
            }

            val cartItems = mutableListOf<CartItem>()

            // Duyệt qua từng item trong giỏ hàng
            for (itemSnapshot in cartSnapshot.children) {
                try {
                    // Lấy key của item (cap003_Xanh Duong_One Size)
                    val itemKey = itemSnapshot.key ?: continue
                    Log.d("CartRepository", "Đang xử lý item với key: $itemKey")

                    // Lấy dữ liệu từ giỏ hàng: đọc trực tiếp các trường con
                    val productId = itemSnapshot.child("productId").getValue(String::class.java)
                    val colorName = itemSnapshot.child("colorName").getValue(String::class.java)
                    val selectedSize = itemSnapshot.child("size").getValue(String::class.java)
                    val quantity = itemSnapshot.child("quantity").getValue(Long::class.java)?.toInt() ?: 1

                    if (productId == null || colorName == null || selectedSize == null) {
                        Log.e("CartRepository", "Dữ liệu thiếu cho item $itemKey")
                        continue
                    }

                    Log.d("CartRepository", "Đọc được thông tin: productId=$productId, colorName=$colorName, size=$selectedSize, quantity=$quantity")

                    // Lấy thông tin sản phẩm từ Realtime Database
                    val productSnapshot = productsRef.child(productId).get().await()
                    if (!productSnapshot.exists()) {
                        Log.e("CartRepository", "Không tìm thấy sản phẩm với ID: $productId")
                        continue
                    }

                    val product = productSnapshot.getValue(Product::class.java)
                    if (product == null) {
                        Log.e("CartRepository", "Không thể chuyển đổi dữ liệu sản phẩm với ID: $productId")
                        continue
                    }

                    Log.d("CartRepository", "Đã lấy được thông tin sản phẩm: ${product.name}")

                    // Tìm màu và size đã chọn
                    val selectedColor = product.colors.find { it.name == colorName }
                    if (selectedColor == null) {
                        Log.e("CartRepository", "Không tìm thấy màu $colorName cho sản phẩm: $productId")
                        continue
                    }

                    val selectedSizeObj = product.sizes.find { it.value == selectedSize }
                    if (selectedSizeObj == null) {
                        Log.e("CartRepository", "Không tìm thấy size $selectedSize cho sản phẩm: $productId")
                        continue
                    }

                    // Kiểm tra tính khả dụng
                    val isColorAvailable = selectedColor.status == "available" && selectedColor.stock > 0
                    val isSizeAvailable = selectedSizeObj.status == "available"

                    Log.d("CartRepository", "Trạng thái khả dụng: màu=${isColorAvailable}, size=${isSizeAvailable}")

                    if (isColorAvailable && isSizeAvailable) {
                        val cartItem = CartItem(
                            product = product,
                            selectedColor = selectedColor,
                            selectedSize = selectedSize,
                            quantity = quantity
                        )
                        cartItems.add(cartItem)
                        Log.d("CartRepository", "Đã thêm sản phẩm ${product.name} vào giỏ hàng")
                    } else {
                        Log.d("CartRepository", "Sản phẩm ${product.name} không khả dụng")
                    }
                } catch (e: Exception) {
                    Log.e("CartRepository", "Lỗi khi xử lý item: ${e.message}", e)
                }
            }

            Log.d("CartRepository", "Hoàn tất, tìm thấy ${cartItems.size} sản phẩm hợp lệ")
            emit(cartItems)
        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi lấy giỏ hàng: ${e.message}", e)
            throw Exception("Không thể lấy giỏ hàng: ${e.message}")
        }
    }

    /**
     * Lấy danh sách sản phẩm trong giỏ hàng (sử dụng ValueEventListener)
     * Trả về Flow để theo dõi thay đổi theo thời gian thực
     *
     * Khác với getUserCart:
     * - Sử dụng callbackFlow để lắng nghe thay đổi realtime
     * - Có cơ chế theo dõi và đếm item đã xử lý xong
     * - Tự động cập nhật khi dữ liệu thay đổi trên Firebase
     *
     * @param userId ID của người dùng
     * @return Flow<List<CartItem>> - luồng dữ liệu thời gian thực chứa danh sách sản phẩm trong giỏ hàng
     */
    fun getUserCartRealtime(userId: String): Flow<List<CartItem>> = callbackFlow {
        Log.d("CartRepository", "Bắt đầu lắng nghe giỏ hàng realtime cho userId: $userId")

        // Tạo listener để lắng nghe thay đổi trong giỏ hàng
        val cartListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("CartRepository", "Có dữ liệu giỏ hàng thay đổi. Tồn tại: ${snapshot.exists()}, có ${snapshot.childrenCount} items")

                if (!snapshot.exists()) {
                    trySend(emptyList())
                    return
                }

                // Tác vụ để theo dõi số lượng item còn lại cần xử lý
                var remainingItems = snapshot.childrenCount.toInt()

                if (remainingItems == 0) {
                    trySend(emptyList())
                    return
                }

                // Danh sách để lưu các item giỏ hàng
                val cartItemsList = mutableListOf<CartItem>()

                // Duyệt qua từng item trong giỏ hàng
                for (itemSnapshot in snapshot.children) {
                    try {
                        // Lấy key của item để debug
                        val itemKey = itemSnapshot.key ?: "unknown"
                        Log.d("CartRepository", "Đang xử lý item với key: $itemKey")

                        // Đọc dữ liệu trực tiếp thay vì dùng Map
                        val productId = itemSnapshot.child("productId").getValue(String::class.java)
                        val colorName = itemSnapshot.child("colorName").getValue(String::class.java)
                        val selectedSize = itemSnapshot.child("size").getValue(String::class.java)
                        val quantity = itemSnapshot.child("quantity").getValue(Long::class.java)?.toInt() ?: 1

                        // Log dữ liệu đã đọc
                        Log.d("CartRepository", "Đọc dữ liệu: productId=$productId, colorName=$colorName, size=$selectedSize, quantity=$quantity")

                        if (productId == null || colorName == null || selectedSize == null) {
                            Log.e("CartRepository", "Dữ liệu giỏ hàng thiếu thông tin cần thiết")
                            remainingItems--
                            if (remainingItems == 0) {
                                trySend(cartItemsList.toList())
                            }
                            continue
                        }

                        // Xử lý không đồng bộ để lấy product
                        productsRef.child(productId).get().addOnSuccessListener { productSnapshot ->
                            try {
                                val product = productSnapshot.getValue(Product::class.java)
                                if (product == null) {
                                    Log.e("CartRepository", "Không thể chuyển đổi dữ liệu sản phẩm: $productId")
                                    remainingItems--
                                    if (remainingItems == 0) {
                                        trySend(cartItemsList.toList())
                                    }
                                    return@addOnSuccessListener
                                }

                                Log.d("CartRepository", "Đã lấy thông tin sản phẩm: ${product.name}")

                                // Tìm màu và size đã chọn
                                val selectedColor = product.colors.find { it.name == colorName }
                                val selectedSizeObj = product.sizes.find { it.value == selectedSize }

                                if (selectedColor == null) {
                                    Log.e("CartRepository", "Không tìm thấy màu $colorName trong sản phẩm ${product.name}")
                                }

                                if (selectedSizeObj == null) {
                                    Log.e("CartRepository", "Không tìm thấy size $selectedSize trong sản phẩm ${product.name}")
                                }

                                if (selectedColor != null && selectedSizeObj != null) {
                                    val isColorAvailable = selectedColor.status == "available" && selectedColor.stock > 0
                                    val isSizeAvailable = selectedSizeObj.status == "available"

                                    Log.d("CartRepository", "Trạng thái khả dụng: màu=${isColorAvailable}, size=${isSizeAvailable}")

                                    if (isColorAvailable && isSizeAvailable) {
                                        val cartItem = CartItem(
                                            product = product,
                                            selectedColor = selectedColor,
                                            selectedSize = selectedSize,
                                            quantity = quantity
                                        )

                                        // Thêm vào danh sách
                                        cartItemsList.add(cartItem)
                                        Log.d("CartRepository", "Đã thêm sản phẩm ${product.name} vào giỏ hàng")
                                    } else {
                                        Log.d("CartRepository", "Sản phẩm ${product.name} không khả dụng")
                                    }
                                } else {
                                    Log.e("CartRepository", "Không tìm thấy màu hoặc size phù hợp cho sản phẩm ${product.name}")
                                }
                            } catch (e: Exception) {
                                Log.e("CartRepository", "Lỗi khi xử lý sản phẩm: ${e.message}", e)
                            } finally {
                                // Giảm số lượng item cần xử lý và kiểm tra nếu đã xử lý hết
                                remainingItems--
                                Log.d("CartRepository", "Còn $remainingItems items cần xử lý")
                                if (remainingItems == 0) {
                                    Log.d("CartRepository", "Đã xử lý tất cả ${cartItemsList.size} sản phẩm trong giỏ hàng")
                                    trySend(cartItemsList.toList())
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("CartRepository", "Lỗi khi lấy sản phẩm: ${exception.message}")

                            // Giảm số lượng item cần xử lý và kiểm tra nếu đã xử lý hết
                            remainingItems--
                            if (remainingItems == 0) {
                                trySend(cartItemsList.toList())
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CartRepository", "Lỗi khi xử lý item giỏ hàng: ${e.message}", e)

                        // Giảm số lượng item cần xử lý và kiểm tra nếu đã xử lý hết
                        remainingItems--
                        if (remainingItems == 0) {
                            trySend(cartItemsList.toList())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartRepository", "Lỗi Firebase: ${error.message}")
                close(error.toException())
            }
        }

        // Đăng ký listener
        val reference = cartRef.child(userId)
        reference.addValueEventListener(cartListener)

        // Hủy đăng ký listener khi Flow bị đóng
        awaitClose {
            reference.removeEventListener(cartListener)
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     *
     * Quy trình:
     * 1. Kiểm tra tính khả dụng của sản phẩm
     * 2. Tạo ID duy nhất cho item trong giỏ hàng (productId_colorName_size)
     * 3. Lưu thông tin vào database
     *
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc đã chọn
     * @param size Kích cỡ đã chọn
     * @param quantity Số lượng (mặc định là 1)
     * @throws Exception nếu có lỗi xảy ra trong quá trình thêm
     */
    suspend fun addToCart(
        userId: String,
        productId: String,
        colorName: String,
        size: String,
        quantity: Int = 1
    ) {
        try {
            Log.d("CartRepository", "Thêm vào giỏ hàng: productId=$productId, colorName=$colorName, size=$size")

            // Kiểm tra tình trạng sản phẩm
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
            cartRef.child(userId).child(itemId).setValue(cartItemData).await()
            Log.d("CartRepository", "Đã thêm sản phẩm vào giỏ hàng thành công")

        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi thêm vào giỏ hàng: ${e.message}")
            throw Exception("Không thể thêm vào giỏ hàng: ${e.message}")
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     *
     * Quy trình:
     * 1. Nếu số lượng <= 0, xóa sản phẩm khỏi giỏ hàng
     * 2. Kiểm tra tính khả dụng của sản phẩm
     * 3. Kiểm tra số lượng tồn kho
     * 4. Cập nhật số lượng trong database
     *
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @param quantity Số lượng mới
     * @throws Exception nếu có lỗi xảy ra trong quá trình cập nhật
     */
    suspend fun updateCartItemQuantity(
        userId: String,
        productId: String,
        colorName: String,
        size: String,
        quantity: Int
    ) {
        try {
            Log.d("CartRepository", "Cập nhật số lượng: productId=$productId, colorName=$colorName, size=$size, quantity=$quantity")

            if (quantity <= 0) {
                removeFromCart(userId, productId, colorName, size)
                return
            }

            // Kiểm tra tình trạng sản phẩm
            val isAvailable = checkProductAvailability(productId, colorName, size)
            if (!isAvailable) {
                throw Exception("Sản phẩm không khả dụng hoặc đã hết hàng")
            }

            // Kiểm tra tồn kho
            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.getValue(Product::class.java)
                ?: throw Exception("Không tìm thấy sản phẩm")

            val color = product.colors.find { it.name == colorName }
                ?: throw Exception("Không tìm thấy màu sắc")

            if (quantity > color.stock) {
                throw Exception("Số lượng vượt quá tồn kho (chỉ còn ${color.stock} sản phẩm)")
            }

            // ID của item trong giỏ hàng
            val itemId = "${productId}_${colorName}_$size"

            // Cập nhật số lượng
            cartRef.child(userId).child(itemId).child("quantity").setValue(quantity).await()
            Log.d("CartRepository", "Đã cập nhật số lượng thành công")

        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi cập nhật số lượng: ${e.message}")
            throw Exception("Không thể cập nhật số lượng: ${e.message}")
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     *
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @throws Exception nếu có lỗi xảy ra trong quá trình xóa
     */
    suspend fun removeFromCart(
        userId: String,
        productId: String,
        colorName: String,
        size: String
    ) {
        try {
            Log.d("CartRepository", "Xóa khỏi giỏ hàng: productId=$productId, colorName=$colorName, size=$size")

            val itemId = "${productId}_${colorName}_$size"
            cartRef.child(userId).child(itemId).removeValue().await()
            Log.d("CartRepository", "Đã xóa sản phẩm khỏi giỏ hàng thành công")

        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi xóa khỏi giỏ hàng: ${e.message}")
            throw Exception("Không thể xóa khỏi giỏ hàng: ${e.message}")
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng sau khi tạo đơn hàng
     *
     * @param userId ID của người dùng
     * @throws Exception nếu có lỗi xảy ra trong quá trình xóa
     */
    suspend fun clearCart(userId: String) {
        try {
            Log.d("CartRepository", "Xóa toàn bộ giỏ hàng cho userId: $userId")

            cartRef.child(userId).removeValue().await()
            Log.d("CartRepository", "Đã xóa giỏ hàng thành công")

        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi xóa giỏ hàng: ${e.message}")
            throw Exception("Không thể xóa giỏ hàng: ${e.message}")
        }
    }

    /**
     * Kiểm tra sản phẩm đã có trong giỏ hàng chưa
     *
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @param colorName Tên màu sắc
     * @param size Kích cỡ
     * @return true nếu sản phẩm đã có trong giỏ hàng, false nếu chưa
     * @throws Exception nếu có lỗi xảy ra trong quá trình kiểm tra
     */
    suspend fun isProductInCart(
        userId: String,
        productId: String,
        colorName: String,
        size: String
    ): Boolean {
        try {
            Log.d("CartRepository", "Kiểm tra sản phẩm trong giỏ hàng: productId=$productId, colorName=$colorName, size=$size")

            val itemId = "${productId}_${colorName}_$size"
            val snapshot = cartRef.child(userId).child(itemId).get().await()
            val exists = snapshot.exists()

            Log.d("CartRepository", "Sản phẩm ${if (exists) "đã" else "chưa"} có trong giỏ hàng")
            return exists

        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi kiểm tra sản phẩm trong giỏ hàng: ${e.message}")
            throw Exception("Không thể kiểm tra sản phẩm: ${e.message}")
        }
    }

    /**
     * Kiểm tra tính khả dụng của sản phẩm
     * Một sản phẩm được coi là khả dụng khi:
     * - Màu sắc có trạng thái "available" và có tồn kho
     * - Kích cỡ có trạng thái "available"
     *
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param sizeValue Giá trị kích cỡ
     * @return true nếu sản phẩm khả dụng, false nếu không khả dụng
     * @throws Exception nếu có lỗi xảy ra trong quá trình kiểm tra
     */
    suspend fun checkProductAvailability(
        productId: String,
        colorName: String,
        sizeValue: String
    ): Boolean {
        try {
            Log.d("CartRepository", "Kiểm tra tính khả dụng của sản phẩm: productId=$productId, colorName=$colorName, sizeValue=$sizeValue")

            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.getValue(Product::class.java)
                ?: throw Exception("Không tìm thấy sản phẩm")

            // Tìm màu đã chọn
            val selectedColor = product.colors.find { it.name == colorName }
                ?: return false

            // Kiểm tra trạng thái màu và tồn kho
            if (selectedColor.status != "available" || selectedColor.stock <= 0) {
                Log.d("CartRepository", "Màu sắc không khả dụng hoặc hết hàng: status=${selectedColor.status}, stock=${selectedColor.stock}")
                return false
            }

            // Tìm size đã chọn
            val selectedSize = product.sizes.find { it.value == sizeValue }
                ?: return false

            // Kiểm tra trạng thái size
            val result = selectedSize.status == "available"
            Log.d("CartRepository", "Kết quả kiểm tra tính khả dụng: $result")
            return result

        } catch (e: Exception) {
            Log.e("CartRepository", "Lỗi khi kiểm tra tình trạng sản phẩm: ${e.message}")
            throw Exception("Không thể kiểm tra tình trạng sản phẩm: ${e.message}")
        }
    }

    /**
     * Lấy số lượng tồn kho của sản phẩm
     *
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @return Số lượng tồn kho của sản phẩm với màu sắc chỉ định
     * @throws Exception nếu không tìm thấy sản phẩm hoặc màu sắc
     */
    suspend fun getProductStock(productId: String, colorName: String): Int {
        try {
            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.getValue(Product::class.java)
                ?: throw Exception("Không tìm thấy sản phẩm")

            val selectedColor = product.colors.find { it.name == colorName }
                ?: throw Exception("Không tìm thấy màu sắc")

            return selectedColor.stock
        } catch (e: Exception) {
            throw Exception("Không thể lấy thông tin tồn kho: ${e.message}")
        }
    }

    /**
     * Đếm số lượng sản phẩm trong giỏ hàng
     *
     * @param userId ID người dùng
     * @return Số lượng sản phẩm (số mục) trong giỏ hàng
     * @throws Exception nếu có lỗi xảy ra trong quá trình đếm
     */
    suspend fun getCartItemCount(userId: String): Int {
        try {
            val snapshot = cartRef.child(userId).get().await()
            return snapshot.childrenCount.toInt()
        } catch (e: Exception) {
            throw Exception("Không thể đếm số lượng sản phẩm trong giỏ hàng: ${e.message}")
        }
    }
}