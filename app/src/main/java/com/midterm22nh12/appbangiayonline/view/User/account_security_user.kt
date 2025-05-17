package com.midterm22nh12.appbangiayonline.view.User

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.midterm22nh12.appbangiayonline.databinding.AccountSecurityUserBinding

class account_security_user(
    private val context: Context,
    private val binding: AccountSecurityUserBinding,
    private val lifecycleOwner: LifecycleOwner
) {
    private val authViewModel = (context as MainActivityUser).getSharedViewModel()
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
            binding.tvAddressAccountSecurityUser.text = user?.address
            binding.etPhoneAccountSecurityUser.setText(user?.phone)
        }
        binding.btSaveProfileAccountSecurityUser.setOnClickListener{
            updateUserProfile()
        }
        //khi người dùng đăng nhập
        if (authViewModel.isUserLoggedIn()) {
            authViewModel.loadCurrentUserInfo()
        }
    }
    private fun updateUserProfile() {
        val fullName = binding.etFirstNameAccountSecurityUser.text.toString() + " " + binding.etLastNameAccountSecurityUser.text.toString()
        val phone = binding.etPhoneAccountSecurityUser.text.toString().trim()
        val email = binding.tvEmailAccountSecurityUser.text.toString().trim()
        val address = binding.tvAddressAccountSecurityUser.text.toString().trim()

        // Kiểm tra các trường không được để trống
        if (binding.etFirstNameAccountSecurityUser.text.isEmpty()) {
            binding.etFirstNameAccountSecurityUser.error = "Vui lòng nhập đue thông tin"
            return
        }
        if (binding.etLastNameAccountSecurityUser.text.isEmpty()) {
            binding.etLastNameAccountSecurityUser.error = "Vui lòng nhập đue thông tin"
            return
        }

        if (phone.isEmpty()) {
            binding.etPhoneAccountSecurityUser.error = "Vui lòng nhập số điện thoại"
            return
        }

        if (email.isEmpty()) {
            binding.tvEmailAccountSecurityUser.error = "Vui lòng nhập email"
            return
        }

        authViewModel.updateUserProfile(fullName, phone, email, address)

        // Thêm observer này để xử lý kết quả
        authViewModel.profileUpdateResult.observe(lifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                if (result.isSuccess) {
                  Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                } else {
                    // Lấy thông tin lỗi cụ thể
                    val errorMessage = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    Toast.makeText(context, "Cập nhật thất bại: $errorMessage", Toast.LENGTH_LONG).show()

                    // In lỗi ra logcat để debug
                    Log.e("ProfileUpdate", "Lỗi cập nhật: $errorMessage", result.exceptionOrNull())
                }
            }
        }
    }
}