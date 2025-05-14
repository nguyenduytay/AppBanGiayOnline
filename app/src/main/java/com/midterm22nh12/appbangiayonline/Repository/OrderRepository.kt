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
    private val orderStatusHistoryRef = database.getReference("order_status_history")
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

            // Tạo bản ghi lịch sử trạng thái đầu tiên
            val statusHistory = OrderStatusHistory(
                orderId = orderId,
                status = "pending",
                timestamp = System.currentTimeMillis(),
                note = "Đơn hàng mới được tạo",
                updatedBy = "system"
            )

            // Tạo ID cho status history
            val historyId = UUID.randomUUID().toString()
            orderStatusHistoryRef.child(historyId).setValue(statusHistory).await()

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
        orderId: String,
        newStatus: String,
        note: String = "",
        updatedBy: String = "system"
    ) {
        try {
            // Cập nhật trạng thái trong đơn hàng
            val updateData = mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            ordersRef.child(orderId).updateChildren(updateData).await()

            // Thêm vào lịch sử trạng thái
            val statusHistory = OrderStatusHistory(
                orderId = orderId,
                status = newStatus,
                timestamp = System.currentTimeMillis(),
                note = note,
                updatedBy = updatedBy
            )

            // Tạo ID cho status history
            val historyId = UUID.randomUUID().toString()
            orderStatusHistoryRef.child(historyId).setValue(statusHistory).await()

            // Nếu đơn hàng bị hủy, cập nhật lại tồn kho
            if (newStatus == "cancelled") {
                restoreProductStock(orderId)
            }
        } catch (e: Exception) {
            throw Exception("Không thể cập nhật trạng thái đơn hàng: ${e.message}")
        }
    }

    /**
     * Khôi phục lại số lượng tồn kho khi đơn hàng bị hủy
     * @param orderId ID đơn hàng bị hủy
     */
    private suspend fun restoreProductStock(orderId: String) {
        try {
            // Lấy danh sách sản phẩm trong đơn hàng
            val itemsSnapshot = orderItemsRef.orderByChild("orderId").equalTo(orderId).get().await()
            if (!itemsSnapshot.exists()) return

            // Duyệt qua từng sản phẩm để khôi phục tồn kho
            for (itemSnapshot in itemsSnapshot.children) {
                val item = itemSnapshot.getValue(OrderItem::class.java) ?: continue

                // Lấy thông tin sản phẩm
                val productSnapshot = productsRef.child(item.productId).get().await()
                val product = productSnapshot.getValue(Product::class.java) ?: continue

                // Tìm và cập nhật số lượng tồn kho của màu sắc
                val updatedColors = product.colors.map { color ->
                    if (color.name == item.color) {
                        // Tính toán số lượng tồn kho mới
                        val newStock = color.stock + item.quantity
                        // Trạng thái mới (nếu có hàng trở lại)
                        val newStatus = if (color.status == "out_of_stock" && newStock > 0) "available" else color.status
                        // Tạo màu mới với số lượng tồn kho đã cập nhật
                        color.copy(stock = newStock, status = newStatus)
                    } else {
                        color
                    }
                }

                // Cập nhật sản phẩm với màu sắc đã cập nhật
                productsRef.child(item.productId).child("colors").setValue(updatedColors).await()
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Không thể khôi phục tồn kho: ${e.message}")
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

            // Thêm vào lịch sử trạng thái
            val statusHistory = OrderStatusHistory(
                orderId = orderId,
                status = "payment_$paymentStatus",
                timestamp = System.currentTimeMillis(),
                note = "Cập nhật trạng thái thanh toán: $paymentStatus",
                updatedBy = "system"
            )

            // Tạo ID cho status history
            val historyId = UUID.randomUUID().toString()
            orderStatusHistoryRef.child(historyId).setValue(statusHistory).await()
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
     * Lấy lịch sử trạng thái của một đơn hàng
     * @param orderId ID của đơn hàng
     * @return Flow chứa danh sách lịch sử trạng thái
     */
    fun getOrderStatusHistory(orderId: String): Flow<List<OrderStatusHistory>> = flow {
        try {
            val snapshot = orderStatusHistoryRef.orderByChild("orderId").equalTo(orderId).get().await()
            val history = mutableListOf<OrderStatusHistory>()

            for (historySnapshot in snapshot.children) {
                val statusHistory = historySnapshot.getValue(OrderStatusHistory::class.java)
                if (statusHistory != null) {
                    history.add(statusHistory)
                }
            }

            // Sắp xếp theo thời gian (giảm dần)
            val sortedHistory = history.sortedByDescending { it.timestamp }
            emit(sortedHistory)
        } catch (e: Exception) {
            throw Exception("Không thể lấy lịch sử trạng thái đơn hàng: ${e.message}")
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
            updateProductRating(review.productId)

            return reviewId
        } catch (e: Exception) {
            throw Exception("Không thể thêm đánh giá sản phẩm: ${e.message}")
        }
    }

    /**
     * Cập nhật điểm đánh giá trung bình của sản phẩm
     * @param productId ID sản phẩm
     */
    private suspend fun updateProductRating(productId: String) {
        try {
            // Lấy tất cả đánh giá của sản phẩm
            val reviewsSnapshot = productReviewsRef.orderByChild("productId").equalTo(productId).get().await()

            if (!reviewsSnapshot.exists()) return

            val reviews = mutableListOf<ProductReview>()
            for (reviewSnapshot in reviewsSnapshot.children) {
                val review = reviewSnapshot.getValue(ProductReview::class.java)
                if (review != null) {
                    reviews.add(review)
                }
            }

            // Nếu không có đánh giá, không cần cập nhật
            if (reviews.isEmpty()) return

            // Tính điểm đánh giá trung bình
            val averageRating = reviews.map { it.rating }.average()

            // Cập nhật điểm đánh giá của sản phẩm
            productsRef.child(productId).child("rating").setValue(averageRating).await()
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

    /**
     * Hủy đơn hàng
     * @param orderId ID của đơn hàng
     * @param cancellationReason Lý do hủy đơn
     * @param cancelledBy ID của người hủy đơn (user hoặc admin)
     */
    suspend fun cancelOrder(
        orderId: String,
        cancellationReason: String,
        cancelledBy: String
    ) {
        try {
            updateOrderStatus(
                orderId = orderId,
                newStatus = "cancelled",
                note = cancellationReason,
                updatedBy = cancelledBy
            )
        } catch (e: Exception) {
            throw Exception("Không thể hủy đơn hàng: ${e.message}")
        }
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái (dành cho Admin)
     * @param status Trạng thái đơn hàng cần lấy
     * @return Flow chứa danh sách đơn hàng
     */
    fun getOrdersByStatus(status: String): Flow<List<Order>> = flow {
        try {
            val snapshot = ordersRef.orderByChild("status").equalTo(status).get().await()

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
            throw Exception("Không thể lấy danh sách đơn hàng theo trạng thái: ${e.message}")
        }
    }

    /**
     * Lấy danh sách đơn hàng theo khoảng thời gian (dành cho Admin)
     * @param startTime Thời gian bắt đầu (timestamp)
     * @param endTime Thời gian kết thúc (timestamp)
     * @return Flow chứa danh sách đơn hàng
     */
    fun getOrdersByTimeRange(startTime: Long, endTime: Long): Flow<List<Order>> = flow {
        try {
            // Lấy tất cả đơn hàng (không có câu truy vấn startAt và endAt như Firestore)
            val snapshot = ordersRef.orderByChild("createdAt").get().await()

            val orders = mutableListOf<Order>()
            for (orderSnapshot in snapshot.children) {
                val order = orderSnapshot.getValue(Order::class.java)
                if (order != null && order.createdAt >= startTime && order.createdAt <= endTime) {
                    orders.add(order)
                }
            }

            // Sắp xếp theo thời gian tạo (giảm dần)
            val sortedOrders = orders.sortedByDescending { it.createdAt }
            emit(sortedOrders)
        } catch (e: Exception) {
            throw Exception("Không thể lấy danh sách đơn hàng theo khoảng thời gian: ${e.message}")
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
                val pendingProducts = mutableListOf<ItemRecyclerViewConfirmation>()
                val pendingOrderIds = mutableMapOf<String, String>()

                // Tìm các orderId có status pending
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        pendingOrderIds[orderSnapshot.key ?: ""] = order.status
                    }
                }

                Log.d("OrderRepository", "Các orderId pending: $pendingOrderIds")

                // Nếu không có orderId pending, trả về list rỗng
                if (pendingOrderIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }

                // Truy vấn toàn bộ order_items
                orderItemsRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(allItemsSnapshot: DataSnapshot) {
                        pendingProducts.clear()

                        for (itemSnapshot in allItemsSnapshot.children) {
                            val orderItem = itemSnapshot.getValue(OrderItem::class.java)

                            // Kiểm tra xem orderItem có thuộc các orderId pending không
                            if (orderItem != null && orderItem.orderId in pendingOrderIds) {
                                val orderStatus = pendingOrderIds[orderItem.orderId]
                                // Chuyển đổi OrderItem sang ItemRecyclerViewConfirmation
                                val pendingProduct = ItemRecyclerViewConfirmation(
                                    orderItemId = orderItem.id,
                                    orderId = orderItem.orderId,
                                    productId = orderItem.productId,
                                    productName = orderItem.productName,
                                    price = orderItem.price,
                                    quantity = orderItem.quantity,
                                    colorName = orderItem.color,
                                    size = orderItem.size,
                                    productImage = orderItem.productImage,
                                    orderDate = System.currentTimeMillis(), // Hoặc lấy từ order nếu có
                                    status = orderStatus ?: "pending"
                                )
                                pendingProducts.add(pendingProduct)
                            }
                        }

                        Log.d("OrderRepository", "Tổng số pending items: ${pendingProducts.size}")
                        trySend(pendingProducts)
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
            orderItemsRef.removeEventListener(listener)
        }
    }

    suspend fun cancelOrderItemByProductId(
        productId: String,
        orderId: String,
        userId: String,
        reason: String
    ) {
        try {
            // Tìm và xóa item theo productId và orderId
            val query = orderItemsRef
                .orderByChild("productId")
                .equalTo(productId)

            val snapshot = query.get().await()

            for (childSnapshot in snapshot.children) {
                // Kiểm tra xem item có thuộc đơn hàng này không
                val orderItemOrderId = childSnapshot.child("orderId").getValue(String::class.java)

                if (orderItemOrderId == orderId) {
                    // Xóa item
                    childSnapshot.ref.removeValue().await()

                    Log.d("OrderRepository", "Đã xóa item với productId: $productId khỏi đơn hàng $orderId")
                }
            }

            // Kiểm tra số lượng items còn lại trong đơn hàng
            val remainingItemsSnapshot = orderItemsRef
                .orderByChild("orderId")
                .equalTo(orderId)
                .get()
                .await()

            // Nếu không còn item
            if (!remainingItemsSnapshot.exists()) {
                // Xóa order_status_history liên quan đến orderId
                val orderStatusHistoryQuery = orderStatusHistoryRef
                    .orderByChild("orderId")
                    .equalTo(orderId)

                val orderStatusHistorySnapshot = orderStatusHistoryQuery.get().await()
                for (historySnapshot in orderStatusHistorySnapshot.children) {
                    historySnapshot.ref.removeValue().await()
                }

                // Xóa order
                ordersRef.child(orderId).removeValue().await()

                Log.d("OrderRepository", "Đã xóa toàn bộ thông tin đơn hàng $orderId")
            }

        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi khi hủy item đơn hàng: ${e.message}")
            throw Exception("Không thể hủy item đơn hàng: ${e.message}")
        }
    }
}