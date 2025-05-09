package com.midterm22nh12.appbangiayonline.view.User

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.midterm22nh12.appbangiayonline.databinding.AccountSecurityUserBinding
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel

class account_security_user(
    private val context: Context,
    private val binding: AccountSecurityUserBinding,
    private val authViewModel: AuthViewModel,
    private val lifecycleOwner: LifecycleOwner
) {
    init {
        setUpView()
    }

    private fun setUpView() {
        binding.ivBackAccountSecurityUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
        //load dữ liệu người dùng
        authViewModel.currentUser.observe(lifecycleOwner) { user ->
            binding.tvNameAccountSecurityUser.text = user?.username
            binding.tvEmailAccountSecurityUser.text = user?.email
        }
        //khi người dùng đăng nhập
        if (authViewModel.isUserLoggedIn()) {
            authViewModel.loadCurrentUserInfo()
        }
    }
}