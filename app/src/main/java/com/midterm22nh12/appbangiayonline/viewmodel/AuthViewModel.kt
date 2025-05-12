package com.midterm22nh12.appbangiayonline.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.midterm22nh12.appbangiayonline.model.Entity.User
import com.midterm22nh12.appbangiayonline.Repository.AuthRepository
import com.midterm22nh12.appbangiayonline.Service.AuthService
import com.midterm22nh12.appbangiayonline.Service.UserService
import com.midterm22nh12.appbangiayonline.Utils.Event

class AuthViewModel : ViewModel() {
    private val authService = AuthService()
    private val userService = UserService()
    private val authRepository = AuthRepository(authService, userService)

    // LiveData cho đăng ký
    private val _registrationResult = MutableLiveData<Event<Result<User>>>()
    val registrationResult: MutableLiveData<Event<Result<User>>>
        get() = _registrationResult

    // LiveData cho đăng nhập
    private val _loginResult = MutableLiveData<Event<Result<User>>>()
    val loginResult: MutableLiveData<Event<Result<User>>>
        get() = _loginResult

    // LiveData cho đăng xuất
    private val _logoutResult = MutableLiveData<Event<Result<Unit>>>()
    val logoutResult: MutableLiveData<Event<Result<Unit>>>
        get() = _logoutResult

    // LiveData cho cập nhật địa chỉ
    private val _addressUpdateResult = MutableLiveData<Event<Result<Unit>>>()
    val addressUpdateResult: LiveData<Event<Result<Unit>>>
        get() = _addressUpdateResult

    // LiveData cho loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean>
        get() = _isLoading

    // LiveData cho tên người dùng
    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    // LiveData cho địa chỉ người dùng
    private val _userAddress = MutableLiveData<String?>()
    val userAddress: LiveData<String?> get() = _userAddress

    // LiveData cho thông tin người dùng đầy đủ
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: MutableLiveData<User?> get() = _currentUser

    // Thêm LiveData cho cập nhật profile
    private val _profileUpdateResult = MutableLiveData<Event<Result<Unit>>>()
    val profileUpdateResult: LiveData<Event<Result<Unit>>>
        get() = _profileUpdateResult

    // Đăng ký
    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        rePassword: String
    ) {
        _isLoading.value = true
        authRepository.registerUser(
            fullName,
            username,
            email,
            phone,
            password,
            rePassword
        ) { result ->
            _isLoading.value = false
            _registrationResult.value = Event(result)
        }
    }

    // Đăng nhập
    fun loginUser(userInput: String, password: String) {
        _isLoading.value = true
        authRepository.loginUser(userInput, password) { result ->
            _isLoading.value = false
            _loginResult.value = Event(result)

            // Nếu đăng nhập thành công, kiểm tra và load thông tin người dùng
            if (result.isSuccess) {
                loadCurrentUserInfo()
            }
        }
    }

    // Kiểm tra người dùng đã đăng nhập chưa
    fun isUserLoggedIn(): Boolean {
        return authService.getCurrentUser() != null
    }

    // Kiểm tra địa chỉ người dùng có trống không
    fun isUserAddressEmpty(): Boolean {
        return _currentUser.value?.address.isNullOrEmpty()
    }

    // Cập nhật địa chỉ người dùng
    fun updateUserAddress(address: String) {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            authRepository.updateUserAddress(currentUser.uid, address) { result ->
                _isLoading.value = false
                _addressUpdateResult.value = Event(result)

                // Nếu cập nhật thành công, tải lại thông tin người dùng
                if (result.isSuccess) {
                    loadCurrentUserInfo()
                }
            }
        } else {
            _addressUpdateResult.value = Event(Result.failure(Exception("Người dùng chưa đăng nhập")))
        }
    }

    // Đăng xuất
    fun logoutUser() {
        // Đăng xuất khỏi Firebase
        authService.logoutUser()
        // Cập nhật LiveData để thông báo đăng xuất thành công
        _logoutResult.value = Event(Result.success(Unit))
        // Xóa thông tin người dùng hiện tại
        _currentUser.value = null
        _userName.value = null
        _userAddress.value = null
    }

    // Kiểm tra người dùng đăng nhập là admin hay user
    fun isAdminUser(callback: (Boolean) -> Unit) {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            Log.d("AdminCheck", "Id: ${currentUser.uid} Email: ${currentUser.email}")
        } else {
            Log.d("AdminCheck", "Id: null")
        }
        if (currentUser != null) {
            userService.getUserById(currentUser.uid, object : UserService.UserDataCallBack {
                override fun onSuccess(user: User) {
                    callback(user.isAdmin)
                    Log.d("AdminCheck", "isAdmin--> ${user.isAdmin}")
                }
                override fun onFailure(errorMessage: String) {
                    callback(false)
                }
            })
        } else {
            callback(false)
        }
    }

    // Phương thức tải tên người dùng
    fun loadUserName() {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            userService.getUserById(currentUser.uid, object : UserService.UserDataCallBack {
                override fun onSuccess(user: User) {
                    _userName.value = user.fullName
                    _isLoading.value = false
                }

                override fun onFailure(errorMessage: String) {
                    _userName.value = null
                    _isLoading.value = false
                }
            })
        } else {
            _userName.value = null
        }
    }

    // Phương thức tải địa chỉ người dùng
    fun loadUserAddress() {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            userService.getUserById(currentUser.uid, object : UserService.UserDataCallBack {
                override fun onSuccess(user: User) {
                    _userAddress.value = user.address
                    _isLoading.value = false
                }

                override fun onFailure(errorMessage: String) {
                    _userAddress.value = null
                    _isLoading.value = false
                }
            })
        } else {
            _userAddress.value = null
        }
    }

    // Phương thức tải toàn bộ thông tin người dùng hiện tại
    fun loadCurrentUserInfo() {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            userService.getUserById(currentUser.uid, object : UserService.UserDataCallBack {
                override fun onSuccess(user: User) {
                    _currentUser.value = user
                    _userName.value = user.fullName
                    _userAddress.value = user.address
                    _isLoading.value = false
                }

                override fun onFailure(errorMessage: String) {
                    // Có thể thêm LiveData để thông báo lỗi nếu cần
                    _isLoading.value = false
                    Log.e("UserInfo", "Failed to load user info: $errorMessage")
                }
            })
        } else {
            // Người dùng chưa đăng nhập
            _currentUser.value = null
            _userName.value = null
            _userAddress.value = null
        }
    }
    // Cập nhật thông tin profile người dùng
    fun updateUserProfile(
        fullName: String = "",
        phone: String = "",
        email: String = "",
        address: String = ""
    ) {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            authRepository.updateUserProfile(
                currentUser.uid,
                fullName,
                phone,
                email,
                address
            ) { result ->
                _isLoading.value = false
                _profileUpdateResult.value = Event(result)

                // Nếu cập nhật thành công, tải lại thông tin người dùng
                if (result.isSuccess) {
                    loadCurrentUserInfo()
                }
            }
        } else {
            _profileUpdateResult.value = Event(Result.failure(Exception("Người dùng chưa đăng nhập")))
        }
    }
}