package com.midterm22nh12.appbangiayonline.Service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.midterm22nh12.appbangiayonline.model.Entity.Order.Order
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderItem
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderStatusHistory

class OrderService {
    private val firestore = FirebaseFirestore.getInstance()
    private val ORDERS_COLLECTION = "orders"
    private val ORDER_ITEMS_COLLECTION = "order_items"
    private val ORDER_STATUS_HISTORY_COLLECTION = "order_status_history"
    private val TAG = "OrderService"

    /**
     * Lưu đơn hàng mới vào Firestore
     */
    fun saveOrder(order: Order, callback: (Result<String>) -> Unit) {
        firestore.collection(ORDERS_COLLECTION).document(order.id)
            .set(order)
            .addOnSuccessListener {
                callback(Result.success(order.id))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving order: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lưu một mục OrderItem
     */
    fun saveOrderItem(orderItem: OrderItem, callback: (Result<String>) -> Unit) {
        firestore.collection(ORDER_ITEMS_COLLECTION).document(orderItem.id)
            .set(orderItem)
            .addOnSuccessListener {
                callback(Result.success(orderItem.id))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving order item: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lưu lịch sử trạng thái đơn hàng
     */
    fun saveOrderStatusHistory(statusHistory: OrderStatusHistory, callback: (Result<String>) -> Unit) {
        val docRef = firestore.collection(ORDER_STATUS_HISTORY_COLLECTION).document()
        val statusWithId = statusHistory.copy(orderId = docRef.id)

        docRef.set(statusWithId)
            .addOnSuccessListener {
                callback(Result.success(docRef.id))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving status history: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    fun updateOrderStatus(orderId: String, newStatus: String, callback: (Result<Unit>) -> Unit) {
        firestore.collection(ORDERS_COLLECTION).document(orderId)
            .update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                callback(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating order status: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lấy đơn hàng theo ID
     */
    fun getOrderById(orderId: String, callback: (Result<Order>) -> Unit) {
        firestore.collection(ORDERS_COLLECTION).document(orderId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val order = document.toObject(Order::class.java)
                    order?.let {
                        callback(Result.success(it))
                    } ?: callback(Result.failure(Exception("Không thể chuyển đổi dữ liệu")))
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
     * Lấy danh sách OrderItem của một đơn hàng
     */
    fun getOrderItemsByOrderId(orderId: String, callback: (Result<List<OrderItem>>) -> Unit) {
        firestore.collection(ORDER_ITEMS_COLLECTION)
            .whereEqualTo("orderId", orderId)
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.toObjects(OrderItem::class.java)
                callback(Result.success(items))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting order items: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lấy lịch sử trạng thái của một đơn hàng
     */
    fun getOrderStatusHistory(orderId: String, callback: (Result<List<OrderStatusHistory>>) -> Unit) {
        firestore.collection(ORDER_STATUS_HISTORY_COLLECTION)
            .whereEqualTo("orderId", orderId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val statusHistory = documents.toObjects(OrderStatusHistory::class.java)
                callback(Result.success(statusHistory))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting order status history: ${e.message}", e)
                callback(Result.failure(e))
            }
    }
}