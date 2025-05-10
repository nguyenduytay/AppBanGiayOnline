package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midterm22nh12.appbangiayonline.model.Entity.Order.Order
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderStatusHistory
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import java.util.*

class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val TAG = "OrderRepository"
    private val ORDERS_COLLECTION = "orders"
    private val ORDER_ITEMS_COLLECTION = "order_items"
    private val ORDER_STATUS_HISTORY_COLLECTION = "order_status_history"
    private val PRODUCTS_COLLECTION = "products"
    private val USERS_COLLECTION = "users"

    /**
     * Tạo đơn hàng mới
     */
    fun createOrder(
        items: List<OrderItem>,
        shippingAddress: String,
        phoneNumber: String,
        paymentMethod: String,
        note: String = "",
        callback: (Result<Order>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        try {
            // Tính toán tổng tiền
            var totalAmount = 0L
            var shippingFee = 30000L  // Phí vận chuyển mặc định

            // Tính tổng tiền hàng
            for (item in items) {
                totalAmount += item.price * item.quantity
            }

            // Tạo đơn hàng mới
            val orderId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val order = Order(
                id = orderId,
                userId = currentUser.uid,
                items = items,
                totalAmount = totalAmount + shippingFee,
                shippingAddress = shippingAddress,
                phoneNumber = phoneNumber,
                paymentMethod = paymentMethod,
                status = "pending",
                createdAt = now,
                updatedAt = now,
                note = note,
                shippingFee = shippingFee,
                discount = 0,
                couponCode = "",
                paymentStatus = "unpaid"
            )

            // Lưu đơn hàng vào Firestore
            firestore.collection(ORDERS_COLLECTION).document(orderId)
                .set(order)
                .addOnSuccessListener {
                    // Lưu từng OrderItem với ID đơn hàng
                    val batch = firestore.batch()

                    items.forEachIndexed { index, item ->
                        val itemId = UUID.randomUUID().toString()
                        val itemWithIds = item.copy(
                            orderId = orderId,
                            id = itemId
                        )
                        val itemRef = firestore.collection(ORDER_ITEMS_COLLECTION).document(itemId)
                        batch.set(itemRef, itemWithIds)

                        // Cập nhật số lượng tồn kho (giảm stock)
                        updateProductStock(item.productId, item.colorImage, item.size, -item.quantity)
                    }

                    // Lưu trạng thái đơn hàng đầu tiên
                    val statusHistory = OrderStatusHistory(
                        orderId = orderId,
                        status = "pending",
                        timestamp = now,
                        note = "Đơn hàng mới được tạo",
                        updatedBy = "system"
                    )
                    val statusRef = firestore.collection(ORDER_STATUS_HISTORY_COLLECTION).document()
                    batch.set(statusRef, statusHistory)

                    batch.commit()
                        .addOnSuccessListener {
                            callback(Result.success(order))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error saving order items: ${e.message}", e)
                            callback(Result.failure(e))
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error creating order: ${e.message}", e)
                    callback(Result.failure(e))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in createOrder: ${e.message}", e)
            callback(Result.failure(e))
        }
    }

    /**
     * Cập nhật số lượng tồn kho
     */
//    private fun updateProductStock(productId: String, colorImage: String, size: String, quantityChange: Int) {
//        firestore.collection(PRODUCTS_COLLECTION).document(productId)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    val product = document.toObject(Product::class.java)
//                    product?.let {
//                        // Cập nhật số lượng trong màu sắc cụ thể
//                        val updatedColors = product.colors.map { color ->
//                            if (color.image == colorImage) {
//                                // Giả sử có trường stock trong ProductColor
//                                color.copy(stock = color.stock + quantityChange)
//                            } else {
//                                color
//                            }
//                        }
//
//                        // Cập nhật lại sản phẩm với số lượng mới
//                        firestore.collection(PRODUCTS_COLLECTION).document(productId)
//                            .update("colors", updatedColors)
//                            .addOnFailureListener { e ->
//                                Log.e(TAG, "Error updating product stock: ${e.message}", e)
//                            }
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e(TAG, "Error getting product: ${e.message}", e)
//            }
//    }

    /**
     * Cập nhật số lượng tồn kho sử dụng Transaction để đảm bảo tính nhất quán
     */
    private fun updateProductStock(productId: String, colorImage: String, size: String, quantityChange: Int) {
        firestore.runTransaction { transaction ->
            // Lấy document sản phẩm trong transaction
            val productRef = firestore.collection(PRODUCTS_COLLECTION).document(productId)
            val productDoc = transaction.get(productRef)

            if (!productDoc.exists()) {
                throw Exception("Sản phẩm không tồn tại")
            }

            val product = productDoc.toObject(Product::class.java)
                ?: throw Exception("Không thể chuyển đổi dữ liệu sản phẩm")

            // Tìm và cập nhật màu sắc
            val updatedColors = product.colors.map { color ->
                if (color.image == colorImage) {
                    // Kiểm tra đủ tồn kho khi giảm số lượng
                    if (quantityChange < 0 && color.stock < Math.abs(quantityChange)) {
                        throw Exception("Không đủ số lượng trong kho: có ${color.stock} nhưng cần ${Math.abs(quantityChange)}")
                    }

                    // Cập nhật số lượng
                    color.copy(stock = color.stock + quantityChange)
                } else {
                    color
                }
            }

            // Cập nhật sản phẩm trong transaction
            transaction.update(productRef, "colors", updatedColors)

            // Transaction sẽ tự động commit nếu thành công hoặc rollback nếu có lỗi
            null
        }.addOnSuccessListener {
            Log.d(TAG, "Cập nhật kho thành công: $productId, $colorImage, $size, $quantityChange")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Lỗi cập nhật kho: ${e.message}", e)

            // Ở đây bạn có thể thêm xử lý khi cập nhật thất bại
            // Ví dụ: thông báo lỗi hoặc thử lại
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng hiện tại
     */
    fun getUserOrders(callback: (Result<List<Order>>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        firestore.collection(ORDERS_COLLECTION)
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val orders = documents.toObjects(Order::class.java)
                callback(Result.success(orders))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user orders: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     */
    fun getOrderDetails(orderId: String, callback: (Result<OrderWithItems>) -> Unit) {
        // Truy vấn đơn hàng
        firestore.collection(ORDERS_COLLECTION).document(orderId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val order = document.toObject(Order::class.java)
                    order?.let {
                        // Lấy các OrderItem của đơn hàng này
                        firestore.collection(ORDER_ITEMS_COLLECTION)
                            .whereEqualTo("orderId", orderId)
                            .get()
                            .addOnSuccessListener { itemDocuments ->
                                val items = itemDocuments.toObjects(OrderItem::class.java)
                                val orderWithItems = OrderWithItems(order, items)
                                callback(Result.success(orderWithItems))
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error getting order items: ${e.message}", e)
                                callback(Result.failure(e))
                            }
                    } ?: callback(Result.failure(Exception("Không tìm thấy đơn hàng")))
                } else {
                    callback(Result.failure(Exception("Không tìm thấy đơn hàng")))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting order: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Hủy đơn hàng
     */
    fun cancelOrder(orderId: String, reason: String, callback: (Result<Unit>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        // Cập nhật trạng thái đơn hàng
        firestore.collection(ORDERS_COLLECTION).document(orderId)
            .update(
                mapOf(
                    "status" to "cancelled",
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                // Thêm mục lịch sử trạng thái mới
                val statusHistory = OrderStatusHistory(
                    orderId = orderId,
                    status = "cancelled",
                    timestamp = System.currentTimeMillis(),
                    note = reason,
                    updatedBy = currentUser.uid
                )

                firestore.collection(ORDER_STATUS_HISTORY_COLLECTION).document()
                    .set(statusHistory)
                    .addOnSuccessListener {
                        // Hoàn trả lại số lượng tồn kho
                        restoreProductStock(orderId)
                        callback(Result.success(Unit))
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding status history: ${e.message}", e)
                        callback(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error cancelling order: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Hoàn trả số lượng tồn kho khi hủy đơn hàng
     */
    private fun restoreProductStock(orderId: String) {
        firestore.collection(ORDER_ITEMS_COLLECTION)
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val item = document.toObject(OrderItem::class.java)
                    // Cộng lại số lượng đã trừ trước đó
                    updateProductStock(item.productId, item.colorImage, item.size, item.quantity)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error restoring product stock: ${e.message}", e)
            }
    }
}
