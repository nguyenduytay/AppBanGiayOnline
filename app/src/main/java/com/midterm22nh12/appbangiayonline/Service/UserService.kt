package com.midterm22nh12.appbangiayonline.Service

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.User

class UserService {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val usernamesRef = database.getReference("usernames")

    interface UserCallBack {
        fun onSuccess()
        fun onFailure(errorMessage: String)
    }

    interface UserDataCallBack {
        fun onSuccess(user: User)
        fun onFailure(errorMessage: String)
    }
    // Interface callback cho việc kiểm tra admin
    interface AdminCheckCallback {
        fun onResult(isAdmin: Boolean)
        fun onFailure(errorMessage: String)
    }

    // Lưu thông tin người dùng với địa chỉ
    fun saveUserInfo(
        uid: String,
        fullName: String,
        userName: String,
        email: String,
        phone: String,
        address: String, // Thêm trường địa chỉ
        isAdmin: Boolean,
        callback: UserCallBack
    ) {
        val usersRef = usersRef.child(uid)

        val userData = HashMap<String, Any>()
        userData["fullName"] = fullName
        userData["username"] = userName
        userData["email"] = email
        userData["phone"] = phone
        userData["address"] = address // Thêm địa chỉ vào dữ liệu người dùng
        userData["isAdmin"] = isAdmin
        userData["createdAt"] = ServerValue.TIMESTAMP

        usersRef.setValue(userData)
            .addOnSuccessListener {
                // Tạo index cho username để tìm kiếm nhanh
                usernamesRef.child(userName).setValue(uid)
                    .addOnSuccessListener {
                        callback.onSuccess()
                    }
                    .addOnFailureListener { e ->
                        callback.onFailure("Lỗi khi lưu username")
                    }
            }
            .addOnFailureListener { e ->
                callback.onFailure("Lỗi khi lưu thông tin người dùng")
            }
    }

    // Cập nhật địa chỉ người dùng
    fun updateUserAddress(userId: String, address: String, callback: UserCallBack) {
        val userRef = usersRef.child(userId)
        userRef.child("address").setValue(address)
            .addOnSuccessListener {
                callback.onSuccess()
            }
            .addOnFailureListener { e ->
                callback.onFailure("Lỗi khi cập nhật địa chỉ: ${e.message}")
            }
    }

    // Kiểm tra username đã tồn tại chưa
    fun checkUsernameExits(username: String, callback: (Boolean) -> Unit) {
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val exists = snapshot.exists()
                    callback(exists)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    // Kiểm tra email đã tồn tại chưa
    fun checkEmailExits(email: String, callBack: (Boolean) -> Unit) {
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val exists = snapshot.exists()
                    callBack(exists)
                }
                override fun onCancelled(error: DatabaseError) {
                    callBack(false)
                }
            })
    }

    // Kiểm tra số điện thoại đã tồn tại
    fun checkPhoneExits(phone: String, callback: (Boolean) -> Unit) {
        usersRef.orderByChild("phone").equalTo(phone)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val exists = snapshot.exists()
                    callback(exists)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    // Lấy thông tin người dùng theo userId
    fun getUserById(userId: String, callback: UserDataCallBack) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    try {
                        val data = snapshot.value as? Map<*, *>
                        if (data != null) {
                            val fullName = data["fullName"] as? String ?: ""
                            val username = data["username"] as? String ?: ""
                            val email = data["email"] as? String ?: ""
                            val phone = data["phone"] as? String ?: ""
                            val address = data["address"] as? String ?: "" // Lấy địa chỉ từ Firebase
                            val isAdmin = data["isAdmin"] as? Boolean ?: false
                            val createdAt = (data["createdAt"] as? Long) ?: 0L

                            val user = User(
                                userId,
                                fullName,
                                username,
                                email,
                                phone,
                                address, // Thêm địa chỉ vào đối tượng User
                                isAdmin,
                                createdAt
                            )
                            callback.onSuccess(user)
                        } else {
                            callback.onFailure("Dữ liệu người dùng không hợp lệ")
                        }
                    } catch (e: Exception) {
                        callback.onFailure("Lỗi khi xử lý dữ liệu: ${e.message}")
                    }
                } else {
                    callback.onFailure("Người dùng không tồn tại")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure("Lỗi khi lấy thông tin người dùng")
            }
        })
    }
    // Cập nhật thông tin profile người dùng
    fun updateUserProfile(
        userId: String,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        callback: UserCallBack
    ) {
        val userRef = usersRef.child(userId)

        // Tạo Map chứa các thông tin cần cập nhật
        val updates = HashMap<String, Any>()

        // Chỉ cập nhật các trường không trống
        if (fullName.isNotEmpty()) {
            updates["fullName"] = fullName
        }

        if (phone.isNotEmpty()) {
            // Kiểm tra số điện thoại có thuộc về người dùng khác không
            checkPhoneExits(phone) { exists ->
                if (exists) {
                    // Nếu số điện thoại đã tồn tại, kiểm tra xem có phải của người dùng hiện tại không
                    getUserById(userId, object : UserDataCallBack {
                        override fun onSuccess(user: User) {
                            if (user.phone == phone) {
                                // Nếu là số điện thoại hiện tại của người dùng, tiếp tục cập nhật
                                continueUpdate(userId, updates, callback)
                            } else {
                                // Nếu số điện thoại thuộc về người dùng khác
                                callback.onFailure("Số điện thoại đã được sử dụng bởi người dùng khác")
                            }
                        }

                        override fun onFailure(errorMessage: String) {
                            callback.onFailure(errorMessage)
                        }
                    })
                    return@checkPhoneExits
                } else {
                    // Nếu số điện thoại chưa tồn tại, thêm vào updates
                    updates["phone"] = phone

                    // Nếu đã kiểm tra số điện thoại, tiếp tục kiểm tra email
                    if (email.isNotEmpty()) {
                        checkEmail(userId, email, updates, address, callback)
                    } else if (address.isNotEmpty()) {
                        updates["address"] = address
                        // Tiến hành cập nhật
                        userRef.updateChildren(updates)
                            .addOnSuccessListener {
                                callback.onSuccess()
                            }
                            .addOnFailureListener { e ->
                                callback.onFailure("Lỗi khi cập nhật thông tin: ${e.message}")
                            }
                    } else {
                        // Tiến hành cập nhật chỉ với fullName và phone
                        userRef.updateChildren(updates)
                            .addOnSuccessListener {
                                callback.onSuccess()
                            }
                            .addOnFailureListener { e ->
                                callback.onFailure("Lỗi khi cập nhật thông tin: ${e.message}")
                            }
                    }
                }
            }
            return
        }

        // Nếu có email cần cập nhật và không cần kiểm tra phone
        if (email.isNotEmpty()) {
            checkEmail(userId, email, updates, address, callback)
            return
        }

        // Nếu có địa chỉ cần cập nhật
        if (address.isNotEmpty()) {
            updates["address"] = address
        }

        // Nếu không cần kiểm tra email hoặc phone, cập nhật ngay
        if (updates.isNotEmpty()) {
            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    callback.onSuccess()
                }
                .addOnFailureListener { e ->
                    callback.onFailure("Lỗi khi cập nhật thông tin: ${e.message}")
                }
        } else {
            // Không có thông tin nào cần cập nhật
            callback.onSuccess()
        }
    }

    // Hàm hỗ trợ để kiểm tra email
    private fun checkEmail(
        userId: String,
        email: String,
        updates: HashMap<String, Any>,
        address: String,
        callback: UserCallBack
    ) {
        checkEmailExits(email) { exists ->
            if (exists) {
                // Nếu email đã tồn tại, kiểm tra xem có phải của người dùng hiện tại không
                getUserById(userId, object : UserDataCallBack {
                    override fun onSuccess(user: User) {
                        if (user.email == email) {
                            // Nếu là email hiện tại của người dùng, tiếp tục cập nhật
                            if (address.isNotEmpty()) {
                                updates["address"] = address
                            }

                            // Tiến hành cập nhật
                            val userRef = usersRef.child(userId)
                            userRef.updateChildren(updates)
                                .addOnSuccessListener {
                                    callback.onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    callback.onFailure("Lỗi khi cập nhật thông tin: ${e.message}")
                                }
                        } else {
                            // Nếu email thuộc về người dùng khác
                            callback.onFailure("Email đã được sử dụng bởi người dùng khác")
                        }
                    }

                    override fun onFailure(errorMessage: String) {
                        callback.onFailure(errorMessage)
                    }
                })
            } else {
                // Nếu email chưa tồn tại, thêm vào updates
                updates["email"] = email

                if (address.isNotEmpty()) {
                    updates["address"] = address
                }

                // Tiến hành cập nhật
                val userRef = usersRef.child(userId)
                userRef.updateChildren(updates)
                    .addOnSuccessListener {
                        callback.onSuccess()
                    }
                    .addOnFailureListener { e ->
                        callback.onFailure("Lỗi khi cập nhật thông tin: ${e.message}")
                    }
            }
        }
    }

    // Hàm hỗ trợ để tiếp tục cập nhật sau khi đã kiểm tra
    private fun continueUpdate(userId: String, updates: HashMap<String, Any>, callback: UserCallBack) {
        val userRef = usersRef.child(userId)

        if (updates.isEmpty()) {
            callback.onSuccess()
            return
        }

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                callback.onSuccess()
            }
            .addOnFailureListener { e ->
                callback.onFailure("Lỗi khi cập nhật thông tin: ${e.message}")
            }
    }
    /**
     * Kiểm tra người dùng có phải là admin không
     * @param userId ID của người dùng cần kiểm tra
     * @param callback Callback trả về kết quả
     */
    fun checkUserIsAdmin(userId: String, callback: AdminCheckCallback) {
        if (userId.isEmpty()) {
            callback.onFailure("User ID không hợp lệ")
            return
        }

        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Lấy giá trị isAdmin từ field trong user
                    val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                    Log.d(TAG, "User $userId is admin: $isAdmin")
                    callback.onResult(isAdmin)
                } else {
                    Log.e(TAG, "User $userId not found")
                    callback.onFailure("Không tìm thấy thông tin người dùng")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error checking admin status: ${error.message}")
                callback.onFailure("Lỗi khi kiểm tra trạng thái admin: ${error.message}")
            }
        })
    }
}