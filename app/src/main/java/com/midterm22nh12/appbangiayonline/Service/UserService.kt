package com.midterm22nh12.appbangiayonline.Service

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

    // lưu thông tin người dùng
    fun saveUserInfo(
        uid: String,
        fullName: String,
        userName: String,
        email: String,
        phone: String,
        isAdmin: Boolean,
        callback: UserCallBack
    ) {
        val usersRef = usersRef.child(uid)

        val userData = HashMap<String, Any>()
        userData["fullName"] = fullName
        userData["username"] = userName
        userData["email"] = email
        userData["phone"] = phone
        userData["isAdmin"] = isAdmin
        userData["createdAt"] = ServerValue.TIMESTAMP

        usersRef.setValue(userData)
            .addOnSuccessListener {
                //tạo index cho username để tìm kiếm nhanh
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
    //kiểm tra username đã tồn tại chưa
    fun checkUsernameExits(username : String, callback :(Boolean) -> Unit) {
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
    //kiểm tra email đã tồn tại chưa
    fun checkEmailExits(email : String, callBack : (Boolean) -> Unit)
    {
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
    //kiểm tra số điện thoại đã tồn tại
    fun checkPhoneExits(phone : String , callback : (Boolean) -> Unit)
    {
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
    //lấy thông tin người dùng theo userId
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
                            val isAdmin = data["isAdmin"] as? Boolean ?: false
                            val createdAt = (data["createdAt"] as? Long) ?: 0L

                            val user =
                                User(userId, fullName, username, email, phone, isAdmin, createdAt)
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
}