package com.midterm22nh12.appbangiayonline.Repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.midterm22nh12.appbangiayonline.Utils.ChatUtils
import com.midterm22nh12.appbangiayonline.model.Entity.Message.ChatAttachment
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Message
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val TAG = "ChatRepository"

    // Collection references
    private val conversationsRef = firestore.collection("conversations")
    private val messagesRef = firestore.collection("messages")
    private val attachmentsRef = firestore.collection("chatAttachments")

    /**
     * Tạo cuộc hội thoại mới
     */
    suspend fun createConversation(title: String, initialMessage: String): Result<Conversation> = suspendCoroutine { continuation ->
        val currentUser = auth.currentUser
        if (currentUser == null) {
            continuation.resumeWithException(Exception("Người dùng chưa đăng nhập"))
            return@suspendCoroutine
        }

        try {
            // Sử dụng getUserFullName thay vì displayName
            getUserFullName(currentUser.uid) { userFullName ->
                // Tạo ID cho cuộc hội thoại mới
                val conversationId = UUID.randomUUID().toString()

                // Tạo đối tượng cuộc hội thoại với tên từ Realtime Database
                val conversation = Conversation(
                    id = conversationId,
                    userId = currentUser.uid,
                    userFullName = userFullName, // Sử dụng userFullName từ callback
                    title = title,
                    adminId = "system",
                    status = "open",
                    lastMessage = initialMessage,
                    lastMessageTime = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis()
                )

                // Lưu cuộc hội thoại vào Firestore
                conversationsRef.document(conversationId)
                    .set(conversation)
                    .addOnSuccessListener {
                        // Tạo tin nhắn đầu tiên với tên từ Realtime Database
                        val message = Message(
                            conversationId = conversationId,
                            senderId = currentUser.uid,
                            senderName = userFullName, // Sử dụng userFullName từ callback
                            content = initialMessage,
                            timestamp = System.currentTimeMillis(),
                            read = false,
                            type = "text"
                        )

                        // Lưu tin nhắn đầu tiên
                        messagesRef.document(message.id)
                            .set(message)
                            .addOnSuccessListener {
                                continuation.resume(Result.success(conversation))
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error saving initial message: ${e.message}", e)
                                continuation.resume(Result.failure(e))
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error creating conversation: ${e.message}", e)
                        continuation.resume(Result.failure(e))
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in createConversation: ${e.message}", e)
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Gửi tin nhắn mới
     */
    suspend fun sendMessage(
        conversationId: String,
        content: String,
        type: String = "text",
        attachmentUri: Uri? = null
    ): Result<Message> = suspendCoroutine { continuation ->
        val currentUser = auth.currentUser
        if (currentUser == null) {
            continuation.resumeWithException(Exception("Người dùng chưa đăng nhập"))
            return@suspendCoroutine
        }

        try {
            // Trước tiên, kiểm tra xem cuộc hội thoại có tồn tại không
            conversationsRef.document(conversationId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Cuộc hội thoại tồn tại, tiếp tục gửi tin nhắn
                        val conversation = document.toObject(Conversation::class.java)

                        // Xác định senderId dựa trên vai trò người dùng hiện tại
                        val isUserSender = currentUser.uid == conversation?.userId
                        val actualSenderId = if (isUserSender) currentUser.uid else "system"

                        // Lấy tên người gửi
                        getUserFullName(currentUser.uid) { userFullName ->
                            // Tên hiển thị: nếu là admin thì hiển thị "Hệ thống", nếu là user thì hiển thị tên thật
                            val senderName = if (isUserSender) userFullName else "Hệ thống"

                            // Xử lý upload file nếu có đính kèm
                            if (attachmentUri != null && type != "text") {
                                // Tạo reference đến vị trí lưu file trên Storage
                                val storageRef = storage.reference.child("chat_attachments/${UUID.randomUUID()}")

                                // Upload file
                                storageRef.putFile(attachmentUri)
                                    .continueWithTask { task ->
                                        if (!task.isSuccessful) {
                                            task.exception?.let { throw it }
                                        }
                                        storageRef.downloadUrl
                                    }
                                    .addOnSuccessListener { downloadUri ->
                                        // Tạo tin nhắn mới với đính kèm
                                        val message = Message(
                                            conversationId = conversationId,
                                            senderId = actualSenderId,
                                            senderName = senderName,
                                            content = content,
                                            timestamp = System.currentTimeMillis(),
                                            read = false,
                                            type = type,
                                            attachmentUrl = downloadUri.toString()
                                        )

                                        // Lưu tin nhắn
                                        saveMessageAndUpdateConversation(message, conversation, continuation)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error uploading attachment: ${e.message}", e)
                                        continuation.resume(Result.failure(e))
                                    }
                            } else {
                                // Tạo tin nhắn thông thường không có đính kèm
                                val message = Message(
                                    conversationId = conversationId,
                                    senderId = actualSenderId,
                                    senderName = senderName,
                                    content = content,
                                    timestamp = System.currentTimeMillis(),
                                    read = false,
                                    type = "text"
                                )

                                // Lưu tin nhắn
                                saveMessageAndUpdateConversation(message, conversation, continuation)
                            }
                        }
                    } else {
                        // Cuộc hội thoại không tồn tại
                        continuation.resume(Result.failure(Exception("Cuộc hội thoại không tồn tại")))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error checking conversation: ${e.message}", e)
                    continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendMessage: ${e.message}", e)
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Helper method để lưu tin nhắn và cập nhật thông tin cuộc hội thoại
     */
    private fun saveMessageAndUpdateConversation(
        message: Message,
        conversation: Conversation?,
        continuation: kotlin.coroutines.Continuation<Result<Message>>
    ) {
        // Lưu tin nhắn vào Firestore
        messagesRef.document(message.id)
            .set(message)
            .addOnSuccessListener {
                // Nếu là file đính kèm, lưu thông tin đính kèm
                if (message.attachmentUrl.isNotEmpty() && message.type != "text") {
                    val attachment = ChatAttachment(
                        messageId = message.id,
                        url = message.attachmentUrl,
                        type = message.type,
                        fileName = "attachment", // Có thể cải thiện để lấy tên file thực
                        uploadedAt = System.currentTimeMillis()
                    )

                    attachmentsRef.document(attachment.id).set(attachment)
                }

                // Cập nhật thông tin cuộc hội thoại (tin nhắn mới nhất, thời gian, số tin chưa đọc)
                val isUserSender = message.senderId != "system"
                val updateData = hashMapOf<String, Any>(
                    "lastMessage" to message.content,
                    "lastMessageTime" to System.currentTimeMillis() // Cập nhật thời gian hiện tại để đảm bảo sắp xếp đúng
                )

                // Cập nhật số tin nhắn chưa đọc
                if (isUserSender) {
                    updateData["unreadByAdmin"] = (conversation?.unreadByAdmin ?: 0) + 1
                } else {
                    updateData["unreadByUser"] = (conversation?.unreadByUser ?: 0) + 1
                }

                // Cập nhật cuộc hội thoại
                conversationsRef.document(message.conversationId)
                    .update(updateData)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(message))
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error updating conversation: ${e.message}", e)
                        // Vẫn trả về thành công vì tin nhắn đã được lưu
                        continuation.resume(Result.success(message))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving message: ${e.message}", e)
                continuation.resume(Result.failure(e))
            }
    }
    /**
     * Helper method để tạo tin nhắn với đính kèm
     */
    private fun createMessageWithAttachment(
        conversationId: String,
        currentUser: FirebaseUser,
        content: String,
        type: String,
        attachmentUrl: String,
        conversation: Conversation?,
        continuation: kotlin.coroutines.Continuation<Result<Message>>
    ) {
        // Sử dụng getUserFullName
        getUserFullName(currentUser.uid) { userFullName ->
            // Tạo tin nhắn mới với tên từ Realtime Database
            val message = Message(
                conversationId = conversationId,
                senderId = currentUser.uid,
                senderName = userFullName, // Sử dụng userFullName từ callback
                content = content,
                timestamp = System.currentTimeMillis(),
                read = false,
                type = type,
                attachmentUrl = attachmentUrl
            )

            // Lưu tin nhắn vào Firestore
            messagesRef.document(message.id)
                .set(message)
                .addOnSuccessListener {
                    // Nếu là file đính kèm, lưu thông tin đính kèm
                    if (attachmentUrl.isNotEmpty() && type != "text") {
                        val attachment = ChatAttachment(
                            messageId = message.id,
                            url = attachmentUrl,
                            type = type,
                            fileName = "attachment", // Có thể cải thiện để lấy tên file thực
                            uploadedAt = System.currentTimeMillis()
                        )

                        attachmentsRef.document(attachment.id).set(attachment)
                    }

                    // Cập nhật thông tin cuộc hội thoại (tin nhắn mới nhất, thời gian, số tin chưa đọc)
                    val isUserSender = currentUser.uid == conversation?.userId
                    val updateData = hashMapOf<String, Any>(
                        "lastMessage" to content,
                        "lastMessageTime" to message.timestamp
                    )

                    // Cập nhật số tin nhắn chưa đọc
                    if (isUserSender) {
                        updateData["unreadByAdmin"] = (conversation?.unreadByAdmin ?: 0) + 1
                    } else {
                        updateData["unreadByUser"] = (conversation?.unreadByUser ?: 0) + 1
                    }

                    conversationsRef.document(conversationId)
                        .update(updateData)
                        .addOnSuccessListener {
                            continuation.resume(Result.success(message))
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating conversation: ${e.message}", e)
                            // Vẫn trả về thành công vì tin nhắn đã được lưu
                            continuation.resume(Result.success(message))
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving message: ${e.message}", e)
                    continuation.resume(Result.failure(e))
                }
        }
    }

    /**
     * Lấy danh sách cuộc hội thoại của người dùng
     */
    fun getUserConversations(callback: (Result<List<Conversation>>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        conversationsRef
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val conversations = documents.toObjects(Conversation::class.java)
                callback(Result.success(conversations))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting user conversations: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lấy danh sách cuộc hội thoại của admin
     */
    fun getAdminConversations(callback: (Result<List<Conversation>>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        // Kiểm tra xem người dùng hiện tại có quyền admin không
        // Trong thực tế, bạn cần kiểm tra role của người dùng (admin hay không)
        val isAdmin = true // Tạm thời giả định người dùng hiện tại là admin

        if (!isAdmin) {
            callback(Result.failure(Exception("Không có quyền truy cập")))
            return
        }

        conversationsRef
            .whereEqualTo("adminId", currentUser.uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val conversations = documents.toObjects(Conversation::class.java)
                callback(Result.success(conversations))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting admin conversations: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lấy danh sách tất cả cuộc hội thoại (dành cho super admin)
     */
    fun getAllConversations(callback: (Result<List<Conversation>>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        // Kiểm tra xem người dùng hiện tại có quyền super admin không
        val isSuperAdmin = true // Tạm thời giả định người dùng hiện tại là super admin

        if (!isSuperAdmin) {
            callback(Result.failure(Exception("Không có quyền truy cập")))
            return
        }

        conversationsRef
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val conversations = documents.toObjects(Conversation::class.java)
                callback(Result.success(conversations))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting all conversations: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lấy tin nhắn trong một cuộc hội thoại
     */
    fun getConversationMessages(conversationId: String, callback: (Result<List<Message>>) -> Unit) {
        messagesRef
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val messages = documents.toObjects(Message::class.java)
                callback(Result.success(messages))
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting conversation messages: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Lắng nghe tin nhắn theo thời gian thực trong một cuộc hội thoại
     */
    fun listenToConversationMessages(conversationId: String, callback: (Result<List<Message>>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        messagesRef
            .whereEqualTo("conversationId", conversationId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen error: ${e.message}", e)
                    callback(Result.failure(e))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    callback(Result.success(messages))

                    // Kiểm tra tin nhắn chưa đọc
                    // Log để debug trạng thái đọc của tin nhắn
                    messages.forEach { message ->
                        Log.d(TAG, "Tin nhắn ${message.id}: read=${message.read}, " +
                                "senderId=${message.senderId}, currentUserId=${currentUser.uid}")
                    }

                    // Tự động đánh dấu tin nhắn là đã đọc khi đang xem
                    val hasUnreadMessages = messages.any {
                        !it.read && it.senderId != currentUser.uid
                    }

                    if (hasUnreadMessages) {
                        Log.d(TAG, "Phát hiện tin nhắn chưa đọc, đánh dấu đã đọc")
                        markMessagesAsReadForUser(conversationId) { _ ->
                            // Không cần xử lý kết quả
                        }
                    }
                }
            }
    }

    /**
     * Đánh dấu tin nhắn đã đọc (cho người dùng)
     */
    fun markMessagesAsReadForUser(conversationId: String, callback: (Result<Unit>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        // Xác định xem người dùng hiện tại là user hay admin
        conversationsRef.document(conversationId)
            .get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    callback(Result.failure(Exception("Cuộc hội thoại không tồn tại")))
                    return@addOnSuccessListener
                }

                val conversation = document.toObject(Conversation::class.java)
                if (conversation == null) {
                    callback(Result.failure(Exception("Không thể chuyển đổi dữ liệu")))
                    return@addOnSuccessListener
                }

                // Xác định người gửi tin nhắn và trường unread cần cập nhật
                val messageSenderId: String
                val unreadField: String

                if (currentUser.uid == conversation.userId) {
                    // Người dùng hiện tại là user, cập nhật tin nhắn của admin
                    messageSenderId = conversation.adminId.ifEmpty { "system" }
                    unreadField = "unreadByUser"
                    Log.d(TAG, "User đang đánh dấu tin nhắn của Admin là đã đọc")
                } else {
                    // Người dùng hiện tại là admin, cập nhật tin nhắn của user
                    messageSenderId = conversation.userId
                    unreadField = "unreadByAdmin"
                    Log.d(TAG, "Admin đang đánh dấu tin nhắn của User là đã đọc")
                }

                // Debug: In ra ID của tin nhắn cần cập nhật
                Log.d(TAG, "Cập nhật trường $unreadField cho cuộc hội thoại $conversationId")
                Log.d(TAG, "Tìm tin nhắn từ người gửi: $messageSenderId")

                // Đặt số tin nhắn chưa đọc về 0
                conversationsRef.document(conversationId)
                    .update(unreadField, 0)
                    .addOnSuccessListener {
                        Log.d(TAG, "Đã cập nhật $unreadField = 0")

                        // Tìm tất cả tin nhắn chưa đọc
                        messagesRef
                            .whereEqualTo("conversationId", conversationId)
                            .whereEqualTo("senderId", messageSenderId)
                            .whereEqualTo("read", false)  // Sửa từ isRead thành read
                            .get()
                            .addOnSuccessListener { documents ->
                                Log.d(TAG, "Tìm thấy ${documents.size()} tin nhắn chưa đọc")

                                // Nếu không có tin nhắn nào cần cập nhật
                                if (documents.isEmpty) {
                                    callback(Result.success(Unit))
                                    return@addOnSuccessListener
                                }

                                // Cập nhật nhiều tin nhắn cùng lúc bằng batch
                                val batch = firestore.batch()
                                var count = 0
                                documents.forEach { doc ->
                                    Log.d(TAG, "Đánh dấu tin nhắn ${doc.id} đã đọc")
                                    batch.update(doc.reference, "read", true)  // Sửa từ isRead thành read
                                    count++
                                }

                                batch.commit()
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Đã cập nhật $count tin nhắn thành đã đọc")

                                        // Tải lại tin nhắn để cập nhật UI
                                        messagesRef
                                            .whereEqualTo("conversationId", conversationId)
                                            .orderBy("timestamp", Query.Direction.ASCENDING)
                                            .get()
                                            .addOnSuccessListener { _ ->
                                                Log.d(TAG, "Tải lại danh sách tin nhắn thành công")
                                                callback(Result.success(Unit))
                                            }
                                            .addOnFailureListener { _ ->
                                                // Vẫn thành công vì đã cập nhật được tin nhắn
                                                callback(Result.success(Unit))
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Lỗi cập nhật batch: ${e.message}", e)
                                        callback(Result.failure(e))
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Lỗi khi tìm tin nhắn chưa đọc: ${e.message}", e)
                                callback(Result.failure(e))
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Lỗi cập nhật số tin nhắn chưa đọc: ${e.message}", e)
                        callback(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Lỗi kiểm tra cuộc hội thoại: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Helper method để đánh dấu tin nhắn đã đọc
     */
    private fun markMessagesAsRead(
        conversationId: String,
        currentUserId: String,
        unreadField: String,
        callback: (Result<Unit>) -> Unit
    ) {
        // Đặt số tin nhắn chưa đọc về 0
        conversationsRef.document(conversationId)
            .update(unreadField, 0)
            .addOnSuccessListener {
                // Lấy các tin nhắn chưa đọc
                messagesRef
                    .whereEqualTo("conversationId", conversationId)
                    .whereNotEqualTo("senderId", currentUserId)
                    .whereEqualTo("read", false)  // <-- Sửa từ isRead thành read
                    .get()
                    .addOnSuccessListener { documents ->
                        // Nếu không có tin nhắn nào cần cập nhật
                        if (documents.isEmpty) {
                            callback(Result.success(Unit))
                            return@addOnSuccessListener
                        }

                        // Cập nhật nhiều tin nhắn cùng lúc bằng batch
                        val batch = firestore.batch()
                        documents.forEach { doc ->
                            batch.update(doc.reference, "read", true)  // <-- Sửa từ isRead thành read
                        }

                        batch.commit()
                            .addOnSuccessListener {
                                callback(Result.success(Unit))
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Batch update failure: ${e.message}", e)
                                callback(Result.failure(e))
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error getting unread messages: ${e.message}", e)
                        callback(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating unread count: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Cập nhật trạng thái cuộc hội thoại
     */
    fun updateConversationStatus(conversationId: String, newStatus: String, callback: (Result<Unit>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        // Kiểm tra quyền và lấy tên admin
        conversationsRef.document(conversationId)
            .get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    callback(Result.failure(Exception("Cuộc hội thoại không tồn tại")))
                    return@addOnSuccessListener
                }

                val conversation = document.toObject(Conversation::class.java)
                if (conversation == null) {
                    callback(Result.failure(Exception("Không thể chuyển đổi dữ liệu")))
                    return@addOnSuccessListener
                }

                // Chỉ admin hoặc super admin mới có thể cập nhật trạng thái
                if (currentUser.uid == conversation.adminId) {
                    // Lấy tên admin để thêm vào thông báo
                    getUserFullName(currentUser.uid) { adminName ->
                        // Cập nhật trạng thái
                        conversationsRef.document(conversationId)
                            .update("status", newStatus)
                            .addOnSuccessListener {
                                // Tạo tin nhắn hệ thống thông báo trạng thái đã thay đổi
                                val statusMessage = "Admin $adminName đã thay đổi trạng thái cuộc hội thoại thành $newStatus"
                                val systemMessage = Message(
                                    conversationId = conversationId,
                                    senderId = "system",
                                    senderName = "Hệ thống",
                                    content = statusMessage,
                                    timestamp = System.currentTimeMillis(),
                                    read = true,
                                    type = "text",
                                    isSystemMessage = true
                                )

                                // Lưu tin nhắn hệ thống
                                messagesRef.document(systemMessage.id)
                                    .set(systemMessage)
                                    .addOnSuccessListener {
                                        // Cập nhật tin nhắn mới nhất trong cuộc hội thoại
                                        conversationsRef.document(conversationId)
                                            .update(
                                                "lastMessage", statusMessage,
                                                "lastMessageTime", systemMessage.timestamp
                                            )
                                            .addOnSuccessListener {
                                                callback(Result.success(Unit))
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Error updating last message: ${e.message}", e)
                                                callback(Result.success(Unit)) // Vẫn thành công vì trạng thái đã được cập nhật
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error saving system message: ${e.message}", e)
                                        callback(Result.success(Unit)) // Vẫn thành công vì trạng thái đã được cập nhật
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating conversation status: ${e.message}", e)
                                callback(Result.failure(e))
                            }
                    }
                } else {
                    callback(Result.failure(Exception("Không có quyền cập nhật trạng thái")))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking conversation: ${e.message}", e)
                callback(Result.failure(e))
            }
    }

    /**
     * Gán admin cho cuộc hội thoại
     */
    fun assignAdminToConversation(conversationId: String, adminId: String, callback: (Result<Unit>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(Result.failure(Exception("Người dùng chưa đăng nhập")))
            return
        }

        // Lấy tên admin từ database
        getUserFullName(adminId) { adminName ->
            // Gán admin kèm theo tên
            val updates = hashMapOf<String, Any>(
                "adminId" to adminId,
                "adminName" to adminName  // Thêm tên admin vào cuộc hội thoại
            )

            conversationsRef.document(conversationId)
                .update(updates)
                .addOnSuccessListener {
                    // Tạo tin nhắn hệ thống thông báo admin đã được gán
                    val systemMessage = Message(
                        conversationId = conversationId,
                        senderId = "system",
                        senderName = "Hệ thống",
                        content = "Admin $adminName đã tham gia cuộc hội thoại", // Thêm tên admin vào nội dung tin nhắn
                        timestamp = System.currentTimeMillis(),
                        read = true,
                        type = "text",
                        isSystemMessage = true
                    )

                    // Lưu tin nhắn hệ thống
                    messagesRef.document(systemMessage.id)
                        .set(systemMessage)
                        .addOnSuccessListener {
                            // Cập nhật tin nhắn mới nhất trong cuộc hội thoại
                            conversationsRef.document(conversationId)
                                .update(
                                    "lastMessage", systemMessage.content,
                                    "lastMessageTime", systemMessage.timestamp
                                )
                                .addOnSuccessListener {
                                    callback(Result.success(Unit))
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error updating last message: ${e.message}", e)
                                    callback(Result.success(Unit)) // Vẫn thành công vì admin đã được gán
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error saving system message: ${e.message}", e)
                            callback(Result.success(Unit)) // Vẫn thành công vì admin đã được gán
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error assigning admin: ${e.message}", e)
                    callback(Result.failure(e))
                }
        }
    }

    /**
     * Lấy ID của người dùng hiện tại (đang đăng nhập)
     * @return String ID của người dùng hiện tại hoặc chuỗi rỗng nếu chưa đăng nhập
     */
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    //hamf lấy tên đầy đủ nguười dùng
    private fun getUserFullName(userId: String, callback: (String) -> Unit) {
        val db = FirebaseDatabase.getInstance()
        val userRef = db.getReference("users").child(userId)

        userRef.child("fullName").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fullName = snapshot.getValue(String::class.java) ?: ""
                if (fullName.isNotEmpty()) {
                    callback(fullName)
                } else {
                    // Fallback nếu không tìm thấy tên
                    callback("User_${userId.takeLast(4)}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Fallback nếu có lỗi
                callback("User_${userId.takeLast(4)}")
            }
        })
    }
    /**
     * Thiết lập listener theo dõi thay đổi của cuộc hội thoại
     * @param callback Lambda được gọi khi có thay đổi trong cuộc hội thoại
     * @return ListenerRegistration để có thể hủy listener khi không cần
     */
    fun setupConversationsRealTimeListener(callback: (Conversation) -> Unit): ListenerRegistration {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Không có người dùng đăng nhập")
            // Trả về một listener rỗng có thể hủy
            return object : ListenerRegistration {
                override fun remove() {}
            }
        }

        // Thiết lập listener mới
        return firestore.collection("conversations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen error: ${e.message}", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (doc in snapshot.documentChanges) {
                        val conversation = doc.document.toObject(Conversation::class.java)
                        conversation.id = doc.document.id // Đảm bảo lưu ID

                        // Chỉ xử lý khi có thay đổi
                        if (doc.type == DocumentChange.Type.MODIFIED) {
                            Log.d(TAG, "Cuộc hội thoại cập nhật: ${conversation.id}, " +
                                    "unreadByAdmin: ${conversation.unreadByAdmin}, " +
                                    "unreadByUser: ${conversation.unreadByUser}")

                            // Gọi callback với cuộc hội thoại đã cập nhật
                            callback(conversation)
                        }
                    }
                }
            }
    }

    /**
     * Thiết lập listener để theo dõi số lượng cuộc hội thoại chưa đọc
     * @return ListenerRegistration để có thể hủy listener khi không cần
     */
    fun setupUnreadConversationsCounter(callback: (Int) -> Unit): ListenerRegistration {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "Không có người dùng đăng nhập")
            callback(0)
            return object : ListenerRegistration {
                override fun remove() {}
            }
        }

        Log.d(TAG, "Người dùng hiện tại: ${currentUser.uid}")

        // Kiểm tra vai trò admin
        val isAdmin = ChatUtils.isCurrentUserAdmin()
        Log.d(TAG, "Người dùng là admin: $isAdmin")

//        if (!isAdmin) {
//            Log.d(TAG, "Người dùng không phải admin, trả về 0")
//            callback(0)
//            return object : ListenerRegistration {
//                override fun remove() {}
//            }
//        }

        Log.d(TAG, "Thiết lập listener cho conversations")

        return firestore.collection("conversations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen error: ${e.message}", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d(TAG, "Nhận snapshot, số documents: ${snapshot.documents.size}")

                    // In ra chi tiết từng cuộc hội thoại
                    snapshot.documents.forEach { doc ->
                        val unreadByAdmin = doc.getLong("unreadByAdmin") ?: 0
                        Log.d(TAG, "Cuộc hội thoại ${doc.id}: unreadByAdmin=$unreadByAdmin")
                    }

                    // Đếm số lượng cuộc hội thoại có unreadByAdmin > 0
                    val count = snapshot.documents.count { doc ->
                        val unreadByAdmin = doc.getLong("unreadByAdmin") ?: 0
                        unreadByAdmin > 0
                    }

                    Log.d(TAG, "Số lượng cuộc hội thoại chưa đọc: $count")
                    callback(count)
                } else {
                    Log.d(TAG, "Snapshot null")
                    callback(0)
                }
            }
    }
}