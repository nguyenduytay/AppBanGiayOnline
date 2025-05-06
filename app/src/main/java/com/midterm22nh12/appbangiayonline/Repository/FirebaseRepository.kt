package com.midterm22nh12.appbangiayonline.Repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Base Repository class để thao tác với Firebase
 */
class FirebaseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Lấy tất cả documents từ một collection
     */
    suspend fun <T> getAll(collection: String, clazz: Class<T>): List<T> {
        return try {
            val querySnapshot = db.collection(collection).get().await()
            querySnapshot.toObjects(clazz)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Lấy document theo ID
     */
    suspend fun <T> getById(collection: String, documentId: String, clazz: Class<T>): T? {
        return try {
            val documentSnapshot = db.collection(collection).document(documentId).get().await()
            documentSnapshot.toObject(clazz)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Thêm document mới
     */
    suspend fun add(collection: String, data: Any): String? {
        return try {
            val documentReference = db.collection(collection).add(data).await()
            documentReference.id
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cập nhật document
     */
    suspend fun update(collection: String, documentId: String, data: Any): Boolean {
        return try {
            db.collection(collection).document(documentId).set(data).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Xóa document
     */
    suspend fun delete(collection: String, documentId: String): Boolean {
        return try {
            db.collection(collection).document(documentId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Query documents theo trường
     */
    suspend fun <T> queryByField(collection: String, field: String, value: Any, clazz: Class<T>): List<T> {
        return try {
            val querySnapshot = db.collection(collection).whereEqualTo(field, value).get().await()
            querySnapshot.toObjects(clazz)
        } catch (e: Exception) {
            emptyList()
        }
    }
}