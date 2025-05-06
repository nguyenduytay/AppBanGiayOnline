package com.midterm22nh12.appbangiayonline.Repository

import android.util.Patterns
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.User
import com.midterm22nh12.appbangiayonline.Service.AuthService
import com.midterm22nh12.appbangiayonline.Service.UserService

class AuthRepository(
    private val authService: AuthService,
    private val userService: UserService
) {

    //đăng kí người dùng mới
    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        rePassword: String,
        callback: (Result<User>) -> Unit
    ) {
        // Các kiểm tra đầu vào cơ bản
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            callback(Result.failure(Exception("Vui lòng nhập đầy đủ thông tin")))
            return
        }
        if (password != rePassword) {
            callback(Result.failure(Exception("Mật khẩu không khớp")))
            return
        }
        if (!isValidEmail(email)) {
            callback(Result.failure(Exception("Định dạng email không hợp lệ")))
            return
        }
        if (!isValidPhone(phone)) {
            callback(Result.failure(Exception("Định dạng số điện thoại không hợp lệ")))
            return
        }

        // Kiểm tra tuần tự
        userService.checkUsernameExits(username) { usernameExists ->
            if (usernameExists) {
                callback(Result.failure(Exception("Username đã tồn tại")))
                return@checkUsernameExits
            }

            userService.checkEmailExits(email) { emailExists ->
                if (emailExists) {
                    callback(Result.failure(Exception("Email đã tồn tại")))
                    return@checkEmailExits
                }

                userService.checkPhoneExits(phone) { phoneExists ->
                    if (phoneExists) {
                        callback(Result.failure(Exception("Số điện thoại đã tồn tại")))
                        return@checkPhoneExits
                    }

                    // Chỉ khi tất cả kiểm tra đều thành công mới tạo tài khoản
                    authService.registerUser(email, password, object : AuthService.AuthCallback {
                        override fun onSuccess(currentUser: FirebaseUser?) {
                            if (currentUser != null) {
                                // Lưu thông tin người dùng
                                userService.saveUserInfo(
                                    currentUser.uid,
                                    fullName,
                                    username,
                                    email,
                                    phone,
                                    false,
                                    object : UserService.UserCallBack {
                                        override fun onSuccess() {
                                            val user = User(
                                                currentUser.uid,
                                                fullName,
                                                username,
                                                email,
                                                phone,
                                                false,
                                                System.currentTimeMillis()
                                            )
                                            callback(Result.success(user))
                                        }

                                        override fun onFailure(errorMessage: String) {
                                            // Nếu lưu thông tin thất bại, xóa tài khoản vừa tạo để tránh rác
                                            try {
                                                currentUser.delete()
                                            } catch (e: Exception) {
                                                // Ignore exception
                                            }
                                            callback(Result.failure(Exception(errorMessage)))
                                        }
                                    }
                                )
                            } else {
                                callback(Result.failure(Exception("Đăng ký thất bại")))
                            }
                        }

                        override fun onFailure(errorMessage: String) {
                            callback(Result.failure(Exception(errorMessage)))
                        }
                    })
                }
            }
        }
    }

    //đăng nhập
    fun loginUser(userInput: String, password: String, callback: (Result<User>) -> Unit) {
        if (userInput.isEmpty() || password.isEmpty()) {
            callback(Result.failure(Exception("Vui lòng nhập đầy đủ thông tin")))
            return
        }
        //xác định xem input là email hay username
        if (isValidEmail(userInput)) {
            loginWithEmail(userInput, password, callback)
        } else {
            findEmailByUserName(userInput) { email ->
                if (email.isNotEmpty()) {
                    loginWithEmail(email, password, callback)
                } else {
                    callback(Result.failure(Exception("Tài khoản không tồn tại")))
                }
            }
        }
    }

    //hàm hỗ trợ đăng nhập
    private fun loginWithEmail(email: String, password: String, callback: (Result<User>) -> Unit) {
        authService.loginUser(email, password, object : AuthService.AuthCallback {
            override fun onSuccess(currentUser: FirebaseUser?) {
                if (currentUser != null) {
                    getUserFromFirebase(currentUser.uid, callback)
                } else {
                    callback(Result.failure(Exception("Đăng nhập thất bại")))
                }
            }

            override fun onFailure(errorMessage: String) {
                callback(Result.failure(Exception(errorMessage)))
            }
        })
    }

    private fun getUserFromFirebase(userId: String, callback: (Result<User>) -> Unit) {
        userService.getUserById(userId, object : UserService.UserDataCallBack {
            override fun onSuccess(user: User) {
                callback(Result.success(user))
            }

            override fun onFailure(errorMessage: String) {
                callback(Result.failure(Exception(errorMessage)))
            }
        })
    }

    private fun findEmailByUserName(username: String, callback: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var found = false
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val email =
                                userSnapshot.child("email").getValue(String::class.java) ?: ""
                            found = true
                            callback(email)
                            break
                        }
                    }

                    if (!found) {
                        callback("")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback("")
                }
            })
    }

    //kiểm tra dạng email hợp lệ
    private fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    //kiểm tra dạng phone hợp lệ
    private fun isValidPhone(phone: String): Boolean {
        val pattern = "^0\\d{9}$".toRegex()
        return pattern.matches(phone)
    }
}