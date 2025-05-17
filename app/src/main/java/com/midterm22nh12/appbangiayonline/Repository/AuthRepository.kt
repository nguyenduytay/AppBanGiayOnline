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

/**
 * Repository xử lý các thao tác xác thực người dùng
 * Kết nối với Firebase Authentication và Realtime Database thông qua các service
 */
class AuthRepository(
    private val authService: AuthService,   // Service xử lý xác thực (đăng ký, đăng nhập)
    private val userService: UserService    // Service quản lý thông tin người dùng trong database
) {

    /**
     * Đăng ký người dùng mới
     *
     * Thực hiện các bước:
     * 1. Kiểm tra tính hợp lệ của đầu vào
     * 2. Kiểm tra xem username, email, phone đã tồn tại chưa
     * 3. Tạo tài khoản Firebase Authentication
     * 4. Lưu thông tin user vào Realtime Database
     *
     * @param fullName Họ tên đầy đủ của người dùng
     * @param username Tên người dùng (dùng để đăng nhập)
     * @param email Email của người dùng
     * @param phone Số điện thoại của người dùng
     * @param password Mật khẩu
     * @param rePassword Xác nhận mật khẩu
     * @param callback Hàm callback trả về kết quả đăng ký (thành công trả về User, thất bại trả về Exception)
     */
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

        // Kiểm tra tuần tự tính duy nhất của username, email và phone
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
                                // Lưu thông tin người dùng với địa chỉ mặc định là rỗng
                                userService.saveUserInfo(
                                    currentUser.uid,
                                    fullName,
                                    username,
                                    email,
                                    phone,
                                    "", // Địa chỉ rỗng
                                    false,
                                    object : UserService.UserCallBack {
                                        override fun onSuccess() {
                                            val user = User(
                                                currentUser.uid,
                                                fullName,
                                                username,
                                                email,
                                                phone,
                                                "", // Địa chỉ rỗng
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

    /**
     * Đăng nhập người dùng
     * Hỗ trợ đăng nhập bằng email hoặc username
     *
     * @param userInput Email hoặc username của người dùng
     * @param password Mật khẩu
     * @param callback Hàm callback trả về kết quả đăng nhập (thành công trả về User, thất bại trả về Exception)
     */
    fun loginUser(userInput: String, password: String, callback: (Result<User>) -> Unit) {
        if (userInput.isEmpty() || password.isEmpty()) {
            callback(Result.failure(Exception("Vui lòng nhập đầy đủ thông tin")))
            return
        }
        // Xác định xem input là email hay username
        if (isValidEmail(userInput)) {
            // Nếu là email, đăng nhập trực tiếp
            loginWithEmail(userInput, password, callback)
        } else {
            // Nếu là username, cần tìm email tương ứng trước
            findEmailByUserName(userInput) { email ->
                if (email.isNotEmpty()) {
                    loginWithEmail(email, password, callback)
                } else {
                    callback(Result.failure(Exception("Tài khoản không tồn tại")))
                }
            }
        }
    }

    /**
     * Cập nhật địa chỉ người dùng
     *
     * @param userId ID của người dùng cần cập nhật
     * @param address Địa chỉ mới
     * @param callback Hàm callback trả về kết quả cập nhật (thành công trả về Unit, thất bại trả về Exception)
     */
    fun updateUserAddress(userId: String, address: String, callback: (Result<Unit>) -> Unit) {
        if (userId.isEmpty()) {
            callback(Result.failure(Exception("ID người dùng không hợp lệ")))
            return
        }
        if (address.isEmpty()) {
            callback(Result.failure(Exception("Vui lòng nhập địa chỉ")))
            return
        }

        userService.updateUserAddress(userId, address, object : UserService.UserCallBack {
            override fun onSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFailure(errorMessage: String) {
                callback(Result.failure(Exception(errorMessage)))
            }
        })
    }

    /**
     * Đăng nhập bằng email
     *
     * @param email Email của người dùng
     * @param password Mật khẩu
     * @param callback Hàm callback trả về kết quả đăng nhập
     */
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

    /**
     * Lấy thông tin User từ Firebase Database dựa trên userId
     *
     * @param userId ID của người dùng
     * @param callback Hàm callback trả về kết quả
     */
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

    /**
     * Tìm email dựa trên username
     *
     * @param username Username cần tìm
     * @param callback Hàm callback trả về email tìm được (rỗng nếu không tìm thấy)
     */
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

    /**
     * Kiểm tra tính hợp lệ của email
     *
     * @param email Email cần kiểm tra
     * @return true nếu email hợp lệ, false nếu không
     */
    private fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    /**
     * Kiểm tra tính hợp lệ của số điện thoại
     * Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số
     *
     * @param phone Số điện thoại cần kiểm tra
     * @return true nếu số điện thoại hợp lệ, false nếu không
     */
    private fun isValidPhone(phone: String): Boolean {
        val pattern = "^0\\d{9}$".toRegex()
        return pattern.matches(phone)
    }

    /**
     * Cập nhật thông tin hồ sơ người dùng
     *
     * @param userId ID của người dùng
     * @param fullName Họ tên đầy đủ mới
     * @param phone Số điện thoại mới
     * @param email Email mới
     * @param address Địa chỉ mới
     * @param callback Hàm callback trả về kết quả cập nhật
     */
    fun updateUserProfile(
        userId: String,
        fullName: String,
        phone: String,
        email: String,
        address: String,
        callback: (Result<Unit>) -> Unit
    ) {
        // Kiểm tra các trường thông tin cần thiết
        if (userId.isEmpty()) {
            callback(Result.failure(Exception("ID người dùng không hợp lệ")))
            return
        }

        // Kiểm tra định dạng email (nếu có)
        if (email.isNotEmpty() && !isValidEmail(email)) {
            callback(Result.failure(Exception("Định dạng email không hợp lệ")))
            return
        }

        // Kiểm tra định dạng số điện thoại (nếu có)
        if (phone.isNotEmpty() && !isValidPhone(phone)) {
            callback(Result.failure(Exception("Định dạng số điện thoại không hợp lệ")))
            return
        }

        // Gọi service để cập nhật thông tin
        userService.updateUserProfile(userId, fullName, phone, email, address, object : UserService.UserCallBack {
            override fun onSuccess() {
                callback(Result.success(Unit))
            }

            override fun onFailure(errorMessage: String) {
                callback(Result.failure(Exception(errorMessage)))
            }
        })
    }
}