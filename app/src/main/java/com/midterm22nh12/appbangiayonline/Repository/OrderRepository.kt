package com.midterm22nh12.appbangiayonline.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.midterm22nh12.appbangiayonline.model.Entity.Order.Order
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderStatusHistory
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.model.Entity.Order.ProductReview
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Repository xử lý các hoạt động liên quan đến đơn hàng
 */
class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")
    private val orderItemsCollection = firestore.collection("order_items")
    private val orderStatusHistoryCollection = firestore.collection("order_status_history")
    private val productReviewsCollection = firestore.collection("product_reviews")
    private val productsCollection = firestore.collection("products")

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

            // Cập nhật đơn hàng với ID mới
            val updatedOrder = order.copy(
                id = orderId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Lưu đơn hàng vào Firestore
            ordersCollection.document(orderId).set(updatedOrder).await()

            // Lưu từng item vào collection riêng với reference tới order
            items.forEach { item ->
                val itemId = if (item.id.isEmpty()) UUID.randomUUID().toString() else item.id
                val updatedItem = item.copy(id = itemId, orderId = orderId)
                orderItemsCollection.document(itemId).set(updatedItem).await()

                // Cập nhật số lượng tồn kho của sản phẩm
                updateProductStock(item.productId, item.color, item.quantity)
            }

            // Tạo bản ghi lịch sử trạng thái đầu tiên
            val initialStatus = OrderStatusHistory(
                orderId = orderId,
                status = "pending",
                timestamp = System.currentTimeMillis(),
                note = "Đơn hàng mới được tạo",
                updatedBy = "system"
            )
            orderStatusHistoryCollection.add(initialStatus).await()

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
            val productDoc = productsCollection.document(productId).get().await()
            val product = productDoc.toObject(Product::class.java) ?: throw Exception("Không tìm thấy sản phẩm")

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
            productsCollection.document(productId).update("colors", updatedColors).await()
        } catch (e: Exception) {
            // Ghi log lỗi nhưng không throw exception để không ảnh hưởng đến quy trình đặt hàng
            println("Không thể cập nhật tồn kho: ${e.message}")
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
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            // Thêm vào lịch sử trạng thái
            val statusHistory = OrderStatusHistory(
                orderId = orderId,
                status = newStatus,
                timestamp = System.currentTimeMillis(),
                note = note,
                updatedBy = updatedBy
            )
            orderStatusHistoryCollection.add(statusHistory).await()

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
            val itemsQuery = orderItemsCollection.whereEqualTo("orderId", orderId).get().await()
            val items = itemsQuery.documents.mapNotNull { it.toObject(OrderItem::class.java) }

            // Duyệt qua từng sản phẩm để khôi phục tồn kho
            items.forEach { item ->
                // Lấy thông tin sản phẩm
                val productDoc = productsCollection.document(item.productId).get().await()
                val product = productDoc.toObject(Product::class.java) ?: return@forEach

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
                productsCollection.document(item.productId).update("colors", updatedColors).await()
            }
        } catch (e: Exception) {
            println("Không thể khôi phục tồn kho: ${e.message}")
        }
    }

    /**
     * Cập nhật trạng thái thanh toán của đơn hàng
     * @param orderId ID của đơn hàng
     * @param paymentStatus Trạng thái thanh toán mới
     */
    suspend fun updatePaymentStatus(orderId: String, paymentStatus: String) {
        try {
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "paymentStatus" to paymentStatus,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            // Thêm vào lịch sử trạng thái
            val statusHistory = OrderStatusHistory(
                orderId = orderId,
                status = "payment_$paymentStatus",
                timestamp = System.currentTimeMillis(),
                note = "Cập nhật trạng thái thanh toán: $paymentStatus",
                updatedBy = "system"
            )
            orderStatusHistoryCollection.add(statusHistory).await()
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
            val orderDoc = ordersCollection.document(orderId).get().await()
            val order = orderDoc.toObject(Order::class.java)
                ?: throw Exception("Không tìm thấy đơn hàng")

            // Lấy danh sách sản phẩm của đơn hàng
            val itemsQuery = orderItemsCollection.whereEqualTo("orderId", orderId).get().await()
            val items = itemsQuery.documents.mapNotNull { it.toObject(OrderItem::class.java) }

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
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(orders)
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
            val snapshot = orderStatusHistoryCollection
                .whereEqualTo("orderId", orderId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val history = snapshot.documents.mapNotNull { it.toObject(OrderStatusHistory::class.java) }
            emit(history)
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

            productReviewsCollection.document(reviewId).set(updatedReview).await()

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
            val reviewsSnapshot = productReviewsCollection
                .whereEqualTo("productId", productId)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.mapNotNull { it.toObject(ProductReview::class.java) }

            // Nếu không có đánh giá, không cần cập nhật
            if (reviews.isEmpty()) return

            // Tính điểm đánh giá trung bình
            val averageRating = reviews.map { it.rating }.average()

            // Cập nhật điểm đánh giá của sản phẩm
            productsCollection.document(productId)
                .update("rating", averageRating)
                .await()
        } catch (e: Exception) {
            println("Không thể cập nhật điểm đánh giá: ${e.message}")
        }
    }

    /**
     * Lấy đánh giá của một sản phẩm
     * @param productId ID của sản phẩm
     * @return Flow chứa danh sách đánh giá
     */
    fun getProductReviews(productId: String): Flow<List<ProductReview>> = flow {
        try {
            val snapshot = productReviewsCollection
                .whereEqualTo("productId", productId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { it.toObject(ProductReview::class.java) }
            emit(reviews)
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
            val snapshot = ordersCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(orders)
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
            val snapshot = ordersCollection
                .whereGreaterThanOrEqualTo("createdAt", startTime)
                .whereLessThanOrEqualTo("createdAt", endTime)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            emit(orders)
        } catch (e: Exception) {
            throw Exception("Không thể lấy danh sách đơn hàng theo khoảng thời gian: ${e.message}")
        }
    }
}