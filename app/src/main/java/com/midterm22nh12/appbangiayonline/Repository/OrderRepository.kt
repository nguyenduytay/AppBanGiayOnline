package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Order.Order
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderStatusHistory
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.model.Entity.Order.ProductReview
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Repository xử lý các hoạt động liên quan đến đơn hàng sử dụng Realtime Database
 */
class OrderRepository {
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("orders")
    private val orderItemsRef = database.getReference("order_items")
    private val productReviewsRef = database.getReference("product_reviews")
    private val productsRef = database.getReference("products")


    /**
     * Tạo đơn hàng mới
     * @param order Đơn hàng cần tạo
     * @param items Danh sách sản phẩm trong đơn hàng
     * @return ID của đơn hàng mới tạo
     */
    suspend fun createOrder(order: Order, items: List<OrderItem>): String {
        try {
            // Tạo ID mới cho đơn hàng nếu chưa có
            val orderId = if (order.id.isEmpty()) UUID.randomUUID().toString() else order.id

            // Cập nhật danh sách items cho đơn hàng
            val updatedItems = items.map { item ->
                val itemId = if (item.id.isEmpty()) UUID.randomUUID().toString() else item.id
                item.copy(id = itemId, orderId = orderId)
            }

            // Cập nhật đơn hàng với ID mới và thời gian
            val updatedOrder = order.copy(
                id = orderId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Lưu đơn hàng vào Realtime Database (không bao gồm items để tránh dữ liệu trùng lặp)
            val orderToSave = updatedOrder.copy(items = emptyList())
            ordersRef.child(orderId).setValue(orderToSave).await()

            // Lưu từng item vào node riêng với reference tới order
            updatedItems.forEach { item ->
                orderItemsRef.child(item.id).setValue(item).await()

                // Cập nhật số lượng tồn kho của sản phẩm
                updateProductStock(item.productId, item.color, item.quantity)

            }

            return orderId
        } catch (e: Exception) {
            throw Exception("Không thể tạo đơn hàng: ${e.message}")
        }
    }

    /**
     * Cập nhật số lượng tồn kho của sản phẩm sau khi đặt hàng
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param quantity Số lượng đã mua
     */
    private suspend fun updateProductStock(productId: String, colorName: String, quantity: Int) {
        try {
            // Lấy thông tin sản phẩm
            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.getValue(Product::class.java) ?: throw Exception("Không tìm thấy sản phẩm")

            // Tìm và cập nhật số lượng tồn kho của màu sắc
            val updatedColors = product.colors.map { color ->
                if (color.name == colorName) {
                    // Tính toán số lượng tồn kho mới
                    val newStock = (color.stock - quantity).coerceAtLeast(0)
                    // Trạng thái mới (nếu hết hàng)
                    val newStatus = if (newStock <= 0) "out_of_stock" else color.status
                    // Tạo màu mới với số lượng tồn kho đã cập nhật
                    color.copy(stock = newStock, status = newStatus)
                } else {
                    color
                }
            }

            // Cập nhật sản phẩm với màu sắc đã cập nhật
            productsRef.child(productId).child("colors").setValue(updatedColors).await()
        } catch (e: Exception) {
            // Ghi log lỗi nhưng không throw exception để không ảnh hưởng đến quy trình đặt hàng
            Log.e("OrderRepository", "Không thể cập nhật tồn kho: ${e.message}")
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng
     * @param orderId ID của đơn hàng
     * @param newStatus Trạng thái mới
     * @param note Ghi chú cho việc cập nhật trạng thái
     * @param updatedBy ID của người dùng thực hiện cập nhật (hoặc "system")
     */
    suspend fun updateOrderStatus(
        orderItemId: String,
        newStatus: String,
    ) {
        try {
            // Cập nhật trạng thái trong đơn hàng
            val updateData = mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            orderItemsRef.child(orderItemId).updateChildren(updateData).await()

        } catch (e: Exception) {
            throw Exception("Không thể cập nhật trạng thái đơn hàng: ${e.message}")
        }
    }

    /**
     * Cập nhật trạng thái thanh toán của đơn hàng
     * @param orderId ID của đơn hàng
     * @param paymentStatus Trạng thái thanh toán mới
     */
    suspend fun updatePaymentStatus(orderId: String, paymentStatus: String) {
        try {
            // Cập nhật trạng thái thanh toán
            val updateData = mapOf(
                "paymentStatus" to paymentStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            ordersRef.child(orderId).updateChildren(updateData).await()
        } catch (e: Exception) {
            throw Exception("Không thể cập nhật trạng thái thanh toán: ${e.message}")
        }
    }

    /**
     * Lấy đơn hàng theo ID kèm danh sách sản phẩm
     * @param orderId ID của đơn hàng
     * @return Đơn hàng và danh sách sản phẩm
     */
    suspend fun getOrderWithItems(orderId: String): OrderWithItems {
        try {
            // Lấy thông tin đơn hàng
            val orderSnapshot = ordersRef.child(orderId).get().await()
            val order = orderSnapshot.getValue(Order::class.java)
                ?: throw Exception("Không tìm thấy đơn hàng")

            // Lấy danh sách sản phẩm của đơn hàng
            val itemsSnapshot = orderItemsRef.orderByChild("orderId").equalTo(orderId).get().await()
            val items = mutableListOf<OrderItem>()

            // Duyệt qua từng sản phẩm
            for (itemSnapshot in itemsSnapshot.children) {
                val item = itemSnapshot.getValue(OrderItem::class.java)
                if (item != null) {
                    items.add(item)
                }
            }

            // Sắp xếp items nếu cần
            // items.sortBy { it.id }

            return OrderWithItems(order, items)
        } catch (e: Exception) {
            throw Exception("Không thể lấy thông tin đơn hàng: ${e.message}")
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     * @param userId ID của người dùng
     * @return Flow chứa danh sách đơn hàng
     */
    fun getUserOrders(userId: String): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersRef.orderByChild("userId").equalTo(userId).get().await()
            val orders = mutableListOf<Order>()

            for (orderSnapshot in snapshot.children) {
                val order = orderSnapshot.getValue(Order::class.java)
                if (order != null) {
                    orders.add(order)
                }
            }

            // Sắp xếp theo thời gian tạo (giảm dần)
            val sortedOrders = orders.sortedByDescending { it.createdAt }
            emit(sortedOrders)
        } catch (e: Exception) {
            throw Exception("Không thể lấy danh sách đơn hàng: ${e.message}")
        }
    }

    /**
     * Thêm đánh giá sản phẩm
     * @param review Đánh giá sản phẩm
     * @return ID của đánh giá
     */
    suspend fun addProductReview(review: ProductReview): String {
        try {
            val reviewId = if (review.id.isEmpty()) UUID.randomUUID().toString() else review.id
            val updatedReview = review.copy(
                id = reviewId,
                createdAt = System.currentTimeMillis()
            )

            productReviewsRef.child(reviewId).setValue(updatedReview).await()

            // Cập nhật điểm đánh giá trung bình của sản phẩm
            updateProductRating(review.productId,review.rating.toDouble())

            return reviewId
        } catch (e: Exception) {
            throw Exception("Không thể thêm đánh giá sản phẩm: ${e.message}")
        }
    }

    /**
     * Cập nhật điểm đánh giá trung bình của sản phẩm
     * @param productId ID sản phẩm
     * @param newRating Điểm đánh giá mới (từ 1-5)
     */
    private suspend fun updateProductRating(productId: String, newRating: Double) {
        try {
            // Kiểm tra giới hạn đầu vào
            val validatedRating = when {
                newRating < 1.0 -> 1.0
                newRating > 5.0 -> 5.0
                else -> newRating
            }

            // Lấy thông tin sản phẩm từ Firebase
            val productSnapshot = productsRef.child(productId).get().await()
            if (!productSnapshot.exists()) return

            // Lấy rating hiện tại, mặc định là 0.0 nếu chưa có
            val currentRating = productSnapshot.child("rating").getValue(Double::class.java) ?: 0.0

            // Tính điểm trung bình mới
            var updatedRating: Double

            if (currentRating == 0.0) {
                // Nếu chưa có đánh giá nào trước đó
                updatedRating = validatedRating
            } else {
                // Sử dụng công thức cân bằng hợp lý:
                // - 95% cho điểm hiện tại (giữ ổn định)
                // - 5% cho đánh giá mới (cập nhật từ từ)
                // Điều này sẽ ngăn chặn đánh giá đơn lẻ ảnh hưởng quá nhiều
                updatedRating = currentRating * 0.90 + validatedRating * 0.5

                // Làm tròn đến 1 chữ số thập phân
                val roundedRating = Math.round(updatedRating * 10.0) / 10.0

                // Giới hạn kết quả trong khoảng 1-5
                updatedRating = when {
                    roundedRating < 1.0 -> 1.0
                    roundedRating > 5.0 -> 5.0
                    else -> roundedRating
                }
            }

            // Cập nhật rating vào database
            productsRef.child(productId).child("rating").setValue(updatedRating).await()
        } catch (e: Exception) {
            Log.e("OrderRepository", "Không thể cập nhật điểm đánh giá: ${e.message}")
        }
    }

    /**
     * Lấy đánh giá của một sản phẩm
     * @param productId ID của sản phẩm
     * @return Flow chứa danh sách đánh giá
     */
    fun getProductReviews(productId: String): Flow<List<ProductReview>> = flow {
        try {
            val snapshot = productReviewsRef.orderByChild("productId").equalTo(productId).get().await()

            val reviews = mutableListOf<ProductReview>()
            for (reviewSnapshot in snapshot.children) {
                val review = reviewSnapshot.getValue(ProductReview::class.java)
                if (review != null) {
                    reviews.add(review)
                }
            }

            // Sắp xếp theo thời gian tạo (giảm dần)
            val sortedReviews = reviews.sortedByDescending { it.createdAt }
            emit(sortedReviews)
        } catch (e: Exception) {
            throw Exception("Không thể lấy đánh giá sản phẩm: ${e.message}")
        }
    }
    fun getPendingOrderItemsRealtime(userId: String): Flow<List<ItemRecyclerViewConfirmation>> = callbackFlow {
        // Truy vấn các đơn hàng của user
        val ordersQuery = ordersRef
            .orderByChild("userId")
            .equalTo(userId)

        // Sử dụng addValueEventListener để lắng nghe realtime
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderItems = mutableListOf<ItemRecyclerViewConfirmation>()
                val userOrderIds = mutableSetOf<String>()
                val orderCreationTimes = mutableMapOf<String, Long>()

                // Tìm tất cả các orderId của user này
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        userOrderIds.add(orderSnapshot.key ?: "")
                        orderCreationTimes[orderSnapshot.key ?: ""] = order.createdAt
                    }
                }

                Log.d("OrderRepository", "Các orderId của user: $userOrderIds")

                // Nếu không có order nào, trả về list rỗng
                if (userOrderIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }

                // Truy vấn toàn bộ order_items
                orderItemsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(allItemsSnapshot: DataSnapshot) {
                        orderItems.clear()

                        for (itemSnapshot in allItemsSnapshot.children) {
                            val orderItem = itemSnapshot.getValue(OrderItem::class.java)

                            // Kiểm tra xem orderItem có thuộc các orderId của user không
                            if (orderItem != null && orderItem.orderId in userOrderIds) {
                                // Lấy thời gian tạo đơn hàng
                                val orderDate = orderCreationTimes[orderItem.orderId] ?: System.currentTimeMillis()

                                // Chuyển đổi OrderItem sang ItemRecyclerViewConfirmation
                                val itemConfirmation = ItemRecyclerViewConfirmation(
                                    orderItemId = orderItem.id,
                                    orderId = orderItem.orderId,
                                    productId = orderItem.productId,
                                    productName = orderItem.productName,
                                    price = orderItem.price,
                                    quantity = orderItem.quantity,
                                    colorName = orderItem.color,
                                    size = orderItem.size,
                                    productImage = orderItem.productImage,
                                    orderDate = orderDate,
                                    status = orderItem.status // Lấy status trực tiếp từ orderItem
                                )
                                orderItems.add(itemConfirmation)
                            }
                        }

                        Log.d("OrderRepository", "Tổng số items: ${orderItems.size}")
                        trySend(orderItems)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("OrderRepository", "Lỗi truy vấn items: ${error.message}")
                        close(error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderRepository", "Lỗi: ${error.message}")
                close(error.toException())
            }
        }

        // Đăng ký listener realtime
        ordersQuery.addValueEventListener(listener)

        // Hủy đăng ký khi flow bị đóng
        awaitClose {
            ordersQuery.removeEventListener(listener)
            orderItemsRef.removeEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    suspend fun cancelOrderItemByProductId(
        productId: String,
        orderId: String,
        colorName: String,
        index: Int
    ) {
        try {
            // Xóa orderItem - Phần 1
            val query = orderItemsRef
                .orderByChild("productId")
                .equalTo(productId)

            val snapshot = query.get().await()
            var itemDeleted = false
            var removedItemPrice = 0L
            var removedItemQuantity = 0

            for (childSnapshot in snapshot.children) {
                val orderItemOrderId = childSnapshot.child("orderId").getValue(String::class.java)
                val orderItemColor = childSnapshot.child("color").getValue(String::class.java)

                if (orderItemOrderId == orderId && orderItemColor == colorName) {
                    // Trước khi xóa, lưu giá và số lượng của sản phẩm để tính toán lại tổng tiền
                    removedItemPrice = childSnapshot.child("price").getValue(Long::class.java) ?: 0L
                    removedItemQuantity = childSnapshot.child("quantity").getValue(Int::class.java) ?: 0

                    childSnapshot.ref.removeValue().await()
                    itemDeleted = true
                    Log.d("OrderRepository", "Đã xóa item với productId: $productId, color: $colorName khỏi đơn hàng $orderId")
                }
            }

            if (!itemDeleted) {
                Log.d("OrderRepository", "Không tìm thấy item với productId: $productId, color: $colorName trong đơn hàng $orderId")
                return
            }

            // Cập nhật lại số sản phẩm trong kho
            updateProductStock(productId, colorName, index)

            // Kiểm tra items còn lại và cập nhật order - Phần 2
            try {
                val remainingItemsSnapshot = orderItemsRef
                    .orderByChild("orderId")
                    .equalTo(orderId)
                    .get()
                    .await()

                // Nếu không còn item, xóa order
                if (!remainingItemsSnapshot.exists()) {
                    try {
                        // Xóa order
                        ordersRef.child(orderId).removeValue().await()
                        Log.d("OrderRepository", "Đã xóa đơn hàng $orderId")
                    } catch (e: Exception) {
                        Log.e("OrderRepository", "Lỗi khi xóa đơn hàng: ${e.message}")
                    }

                    Log.d("OrderRepository", "Đã xóa toàn bộ thông tin đơn hàng $orderId")
                } else {
                    // Nếu còn item, cập nhật lại totalAmount và shippingFee
                    val orderSnapshot = ordersRef.child(orderId).get().await()
                    if (orderSnapshot.exists()) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            // Tính toán lại tổng tiền đơn hàng sau khi xóa item
                            val oldTotalAmount = order.totalAmount
                            val removedAmount = removedItemPrice * removedItemQuantity
                            val newSubtotal = oldTotalAmount - removedAmount - order.shippingFee + order.discount

                            // Tính phí vận chuyển mới dựa trên tổng tiền mới
                            val newShippingFee = calculateShippingFee(newSubtotal)

                            // Tính tổng tiền mới (đã trừ item bị xóa, cộng phí vận chuyển mới và trừ giảm giá)
                            val newTotalAmount = newSubtotal + newShippingFee - order.discount

                            // Cập nhật lại thông tin đơn hàng
                            val updates = hashMapOf<String, Any>(
                                "totalAmount" to newTotalAmount,
                                "shippingFee" to newShippingFee,
                                "updatedAt" to System.currentTimeMillis()
                            )

                            ordersRef.child(orderId).updateChildren(updates).await()
                            Log.d("OrderRepository", "Đã cập nhật đơn hàng $orderId: tổng tiền = $newTotalAmount, phí ship = $newShippingFee")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("OrderRepository", "Lỗi khi kiểm tra và cập nhật đơn hàng: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi khi hủy item đơn hàng: ${e.message}")
            throw Exception("Không thể hủy item đơn hàng: ${e.message}")
        }
    }

    /**
     * Tính phí vận chuyển dựa trên tổng tiền đơn hàng
     * @param totalAmount Tổng tiền đơn hàng
     * @return Phí vận chuyển
     */
    private fun calculateShippingFee(totalAmount: Long): Long {
        // Logic tính phí vận chuyển
        return when {
            totalAmount >= 1000000 -> 0L      // Miễn phí vận chuyển cho đơn từ 1 triệu
            totalAmount >= 500000 -> 15000L   // Giảm phí cho đơn từ 500k
            else -> 30000L                     // Phí mặc định
        }
    }
    /**
     * Cập nhật trạng thái của một OrderItem cụ thể
     * @param orderItemId ID của OrderItem cần cập nhật
     * @param newStatus Trạng thái mới (pending, shipping, delivered, cancelled, evaluate)
     * @return Kết quả thành công hay không
     */
    suspend fun updateOrderItemStatus(orderItemId: String, newStatus: String): Boolean {
        return try {
            // Cập nhật trạng thái của item
            val updateData = mapOf(
                "status" to newStatus,
                // Có thể thêm updateAt nếu OrderItem có trường này
            )
            orderItemsRef.child(orderItemId).updateChildren(updateData).await()

            // Kiểm tra xem tất cả các item trong đơn hàng có cùng trạng thái không
            val itemSnapshot = orderItemsRef.child(orderItemId).get().await()
            val item = itemSnapshot.getValue(OrderItem::class.java)

            if (item != null) {
                val orderId = item.orderId

                // Lấy tất cả các item của đơn hàng này
                val allItemsSnapshot = orderItemsRef.orderByChild("orderId").equalTo(orderId).get().await()
                var allSameStatus = true

                // Kiểm tra xem tất cả item có cùng trạng thái không
                for (otherItemSnapshot in allItemsSnapshot.children) {
                    val otherItem = otherItemSnapshot.getValue(OrderItem::class.java)
                    if (otherItem != null && otherItem.status != newStatus) {
                        allSameStatus = false
                        break
                    }
                }

                // Nếu tất cả các item có cùng trạng thái, cập nhật trạng thái của đơn hàng
                if (allSameStatus) {
                    updateOrderStatus(orderId, newStatus)
                }

                // Nếu trạng thái là cancelled, cập nhật lại số lượng tồn kho
                if (newStatus == "cancelled") {
                    restoreItemStock(item.productId, item.color, item.quantity)
                }
            }

            true
        } catch (e: Exception) {
            Log.e("OrderRepository", "Không thể cập nhật trạng thái item: ${e.message}")
            false
        }
    }

    /**
     * Khôi phục lại số lượng tồn kho cho một sản phẩm cụ thể
     * @param productId ID sản phẩm
     * @param colorName Tên màu sắc
     * @param quantity Số lượng cần khôi phục
     */
    private suspend fun restoreItemStock(productId: String, colorName: String, quantity: Int) {
        try {
            // Lấy thông tin sản phẩm
            val productSnapshot = productsRef.child(productId).get().await()
            val product = productSnapshot.getValue(Product::class.java) ?: return

            // Tìm và cập nhật số lượng tồn kho của màu sắc
            val updatedColors = product.colors.map { color ->
                if (color.name == colorName) {
                    // Tính toán số lượng tồn kho mới
                    val newStock = color.stock + quantity
                    // Trạng thái mới (nếu có hàng trở lại)
                    val newStatus = if (color.status == "out_of_stock" && newStock > 0) "available" else color.status
                    // Tạo màu mới với số lượng tồn kho đã cập nhật
                    color.copy(stock = newStock, status = newStatus)
                } else {
                    color
                }
            }

            // Cập nhật sản phẩm với màu sắc đã cập nhật
            productsRef.child(productId).child("colors").setValue(updatedColors).await()
        } catch (e: Exception) {
            Log.e("OrderRepository", "Không thể khôi phục tồn kho cho item: ${e.message}")
        }
    }
    /**
     * Lấy tất cả các đơn hàng kèm theo các OrderItem của chúng, ngoại trừ đơn hàng có trạng thái "evaluate"
     * @return Danh sách OrderWithItems (mỗi cặp chứa một Order và các OrderItem liên quan)
     */
    suspend fun getAllOrdersWithItems(): List<OrderWithItems> {
        try {
            val result = mutableListOf<OrderWithItems>()

            Log.d("OrderDebug", "Bắt đầu lấy danh sách đơn hàng...")
            val ordersSnapshot = ordersRef.get().await()
            Log.d("OrderDebug", "Tổng số đơn hàng lấy được: ${ordersSnapshot.childrenCount}")

            // Lấy tất cả order items một lần (để tránh lỗi indexOn)
            val allItemsSnapshot = orderItemsRef.get().await()
            val allOrderItems = mutableMapOf<String, MutableList<OrderItem>>()

            // Nhóm các OrderItem theo orderId
            for (itemSnapshot in allItemsSnapshot.children) {
                val item = itemSnapshot.getValue(OrderItem::class.java)
                if (item != null) {
                    if (!allOrderItems.containsKey(item.orderId)) {
                        allOrderItems[item.orderId] = mutableListOf()
                    }
                    allOrderItems[item.orderId]?.add(item)
                }
            }

            Log.d("OrderDebug", "Đã tổ chức ${allOrderItems.size} nhóm OrderItems theo orderId")

            for (orderSnapshot in ordersSnapshot.children) {
                val order = orderSnapshot.getValue(Order::class.java)
                Log.d("OrderDebug", "Đang xử lý đơn hàng: ${order?.id}")

                if (order != null) {
                    // Lấy các OrderItems cho đơn hàng này
                    val items = allOrderItems[order.id] ?: mutableListOf()
                    Log.d("OrderDebug", "  -> Đã lấy ${items.size} OrderItems cho đơn hàng: ${order.id}")

                    // Kiểm tra xem đơn hàng có OrderItem nào ở trạng thái "evaluate" không
                    val hasPendingItems = items.any { it.status == "pending" }
                    val hasShippingItems = items.any { it.status == "shipping" }
                    val hasProcessingItems = items.any { it.status == "processing" }
                    val hasDeliveredItems = items.any { it.status == "delivered" }
                    val hasEvaluatedItems = items.any { it.status == "evaluate" }

                    // Nếu không có OrderItem nào ở trạng thái "evaluate", thêm vào kết quả
                    if (hasPendingItems || hasShippingItems || hasProcessingItems ||(hasDeliveredItems && order.paymentStatus == "unpaid") || (hasEvaluatedItems && order.paymentStatus == "unpaid")) {
                        result.add(OrderWithItems(order, items))
                        Log.d("OrderDebug", "  -> Đã thêm OrderWithItems cho đơn hàng ${order.id}, số lượng item: ${items.size}")
                    }else if (items.isEmpty()) {
                        Log.d("OrderDebug", "  -> Bỏ qua đơn hàng ${order.id} vì không có OrderItem nào")
                    }
                }
                else {
                    Log.e("OrderDebug", "Lỗi: Không thể chuyển đổi orderSnapshot sang Order")
                }
            }

            Log.d("OrderDebug", "Sắp xếp danh sách đơn hàng theo thời gian tạo...")
            return result.sortedByDescending { it.order.createdAt }
        } catch (e: Exception) {
            Log.e("OrderDebug", "Lỗi khi lấy danh sách đơn hàng: ${e.message}", e)
            throw Exception("Không thể lấy danh sách đơn hàng: ${e.message}")
        }
    }
    /**
     * Lấy tất cả các đơn hàng kèm theo các OrderItem của chúng, ngoại trừ đơn hàng có trạng thái "evaluate"
     * @return Danh sách OrderWithItems (mỗi cặp chứa một Order và các OrderItem liên quan)
     */
    suspend fun getAllOrdersWithItemsRevenue(): List<OrderWithItems> {
        try {
            val result = mutableListOf<OrderWithItems>()

            Log.d("OrderDebug-tay", "Bắt đầu lấy danh sách đơn hàng...")
            val ordersSnapshot = ordersRef.get().await()
            Log.d("OrderDebug-tay", "Tổng số đơn hàng lấy được: ${ordersSnapshot.childrenCount}")

            // Lấy tất cả order items một lần (để tránh lỗi indexOn)
            val allItemsSnapshot = orderItemsRef.get().await()
            val allOrderItems = mutableMapOf<String, MutableList<OrderItem>>()

            // Nhóm các OrderItem theo orderId
            for (itemSnapshot in allItemsSnapshot.children) {
                val item = itemSnapshot.getValue(OrderItem::class.java)
                if (item != null) {
                    if (!allOrderItems.containsKey(item.orderId)) {
                        allOrderItems[item.orderId] = mutableListOf()
                    }
                    allOrderItems[item.orderId]?.add(item)
                }
            }

            Log.d("OrderDebug-tay", "Đã tổ chức ${allOrderItems.size} nhóm OrderItems theo orderId")

            for (orderSnapshot in ordersSnapshot.children) {
                val order = orderSnapshot.getValue(Order::class.java)
                Log.d("OrderDebug-tay", "Đang xử lý đơn hàng: ${order?.id}")

                if (order != null) {
                    // Lấy các OrderItems cho đơn hàng này
                    val items = allOrderItems[order.id] ?: mutableListOf()
                    Log.d("OrderDebug-tay", "  -> Đã lấy ${items.size} OrderItems cho đơn hàng: ${order.id}")
                    // Nếu không có OrderItem nào ở trạng thái "evaluate", thêm vào kết quả
                    if (items.isNotEmpty()) {
                        result.add(OrderWithItems(order, items))
                        Log.d("OrderDebug-tay", "  -> Đã thêm OrderWithItems cho đơn hàng ${order.id}, số lượng item: ${items.size}")
                    }else if (items.isEmpty()) {
                        Log.d("OrderDebug-tay", "  -> Bỏ qua đơn hàng ${order.id} vì không có OrderItem nào")
                    }
                }
                else {
                    Log.e("OrderDebug-tay", "Lỗi: Không thể chuyển đổi orderSnapshot sang Order")
                }
            }

            Log.d("OrderDebug", "Sắp xếp danh sách đơn hàng theo thời gian tạo...")
            return result.sortedByDescending { it.order.createdAt }
        } catch (e: Exception) {
            Log.e("OrderDebug", "Lỗi khi lấy danh sách đơn hàng: ${e.message}", e)
            throw Exception("Không thể lấy danh sách đơn hàng: ${e.message}")
        }
    }
}