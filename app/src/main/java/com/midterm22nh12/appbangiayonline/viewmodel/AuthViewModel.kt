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

    // live data cho đăng ký
    private val _registrationResult = MutableLiveData<Event<Result<User>>>()
    val registrationResult: MutableLiveData<Event<Result<User>>>
        get() = _registrationResult

    //live data cho đăng nhập
    private val _loginResult = MutableLiveData<Event<Result<User>>>()
    val loginResult: MutableLiveData<Event<Result<User>>>
        get() = _loginResult

    //live data cho đăng xuất
    private val _logoutResult = MutableLiveData<Event<Result<Unit>>>()
    val logoutResult: MutableLiveData<Event<Result<Unit>>>
        get() = _logoutResult

    //LiveData cho loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean>
        get() = _isLoading

    // LiveData cho tên người dùng
    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    // Thêm LiveData cho thông tin người dùng đầy đủ
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: MutableLiveData<User?> get() = _currentUser

    //đăng ký
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

    //đăng nhập
    fun loginUser(userInput: String, password: String) {
        _isLoading.value = true
        authRepository.loginUser(userInput, password) { result ->
            _isLoading.value = false
            _loginResult.value = Event(result)
        }
    }

    //kiểm tra người dùng đẵ đăng nhập chưa
    fun isUserLoggedIn(): Boolean {
        return authService.getCurrentUser() != null
    }

    //đăng xuất
    fun logoutUser() {
        //đăng xuất khỏi firebase
        authService.logoutUser()
        //cạp nhật live data đẻ thông báo đăng xuất thành công
        _logoutResult.value = Event(Result.success(Unit))
    }

    //kiểm tra người dùng đăng nhập là admin hay user
    fun isAdminUser(callback: (Boolean) -> Unit) {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            Log.d("AdminCheck", "Id: ${currentUser.uid} Email: ${currentUser.email}")
        }
        else
        {
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
    // Phương thức tải toàn bộ thông tin người dùng hiện tại
    fun loadCurrentUserInfo() {
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            _isLoading.value = true
            userService.getUserById(currentUser.uid, object : UserService.UserDataCallBack {
                override fun onSuccess(user: User) {
                    _currentUser.value = user
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
        }
    }
}