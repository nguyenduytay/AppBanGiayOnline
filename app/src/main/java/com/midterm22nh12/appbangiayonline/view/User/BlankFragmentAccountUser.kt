package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankAccountUserBinding
import com.midterm22nh12.appbangiayonline.Utils.LocationUtil
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel

class BlankFragmentAccountUser : Fragment() {
    private lateinit var bindingFragmentBlankAccountUser: FragmentBlankAccountUserBinding
    private lateinit var authViewModel: AuthViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentBlankAccountUser= FragmentBlankAccountUserBinding.inflate(inflater, container, false)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        //hiển thị màn hình tài khoản đầu tiên
        bindingFragmentBlankAccountUser.viewFlipperAccountUser.displayedChild = 0
        //sự kiện chuyển trang
        turnPageAccountUser()
        //sự kiện đăng xuất
        logout()
        //hiển thị tên tài khoản người dùng hiện tại
        setUpAccountUser()
        //lấy địa chỉ của tôi
        getAddress()
        return bindingFragmentBlankAccountUser.root
    }
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationUtil.getAddressToTextView(this, bindingFragmentBlankAccountUser.includeAccountStepUser.tvAddressAccountSetupUser, true)
            } else {
                bindingFragmentBlankAccountUser.includeAccountStepUser.tvAddressAccountSetupUser.text = "Không có quyền truy cập vị trí"
                Toast.makeText(
                    requireContext(),
                    "Yêu cầu cấp quyền vị trí để lấy địa chỉ",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    override fun onPause() {
        super.onPause()
        // Dừng cập nhật vị trí khi fragment không hiển thị
        LocationUtil.stopLocationUpdates()
    }
    //lấy địa chỉ
    private fun getAddress()
    {
        bindingFragmentBlankAccountUser.includeAccountStepUser.ivAddressAccountSetupUser.setOnClickListener{
            LocationUtil.getAddressToTextView(this, bindingFragmentBlankAccountUser.includeAccountStepUser.tvAddressAccountSetupUser, false)
        }
    }

    //sự kiên chuyển trang
    private fun turnPageAccountUser()
    {
        bindingFragmentBlankAccountUser.includeAccountUser.ivSettingAccountUser.setOnClickListener{
            bindingFragmentBlankAccountUser.viewFlipperAccountUser.displayedChild = 1
        }
        bindingFragmentBlankAccountUser.includeAccountStepUser.ivBackAccountSetupUser.setOnClickListener{
            bindingFragmentBlankAccountUser.viewFlipperAccountUser.displayedChild = 0
        }
        bindingFragmentBlankAccountUser.includeAccountUser.ivBackAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.navigateFromOverlayToFragment(1)
        }
        bindingFragmentBlankAccountUser.includeAccountUser.ivCartAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.navigateFromOverlayToFragment(0)
        }
        bindingFragmentBlankAccountUser.includeAccountUser.ivChatAccountUser.setOnClickListener {
            (activity as? MainActivityUser)?.also {
                if (!it.isDrawerOpen()) {
                    it.showMessagesOverlay()
                }
            }
        }
        bindingFragmentBlankAccountUser.includeAccountUser.ivPurchaseHistoryAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.also {
                if (!it.isDrawerOpen()) {
                    it.showPurchaseHistoryUser()
                }
            }
        }
        bindingFragmentBlankAccountUser.includeAccountUser.llConfirmationUserAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.showConfirmationUser()
        }
        bindingFragmentBlankAccountUser.includeAccountUser.llTransportationUserAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.showTransportationUser()
        }
        bindingFragmentBlankAccountUser.includeAccountUser.llMyReviewUserAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.showMyReviewUser()
        }
        bindingFragmentBlankAccountUser.includeAccountStepUser.llAccountPasswordAccountSetupUser.setOnClickListener{
            (activity as? MainActivityUser)?.showAccountSecurityUser()
        }

    }
    //sự kiện đăng xuất tài khoản
    private fun logout()
    {
        bindingFragmentBlankAccountUser.includeAccountStepUser.btLogoutAccountSetupUser.setOnClickListener{
            (activity as? MainActivityUser)?.logout()
        }
    }
    private fun setUpAccountUser()
    {
        authViewModel.userName.observe(viewLifecycleOwner) { userName ->
            bindingFragmentBlankAccountUser.includeAccountUser.tvUsernameAccountUser.text = userName
        }
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            bindingFragmentBlankAccountUser.includeAccountStepUser.tvFullNameAccountSetupUser.text = user?.fullName
            bindingFragmentBlankAccountUser.includeAccountStepUser.tvEmailAccountSetupUser.text = user?.email
            bindingFragmentBlankAccountUser.includeAccountStepUser.tvPhoneAccountSetupUser.text = user?.phone
        }
        if (authViewModel.isUserLoggedIn()) {
            authViewModel.loadUserName()
            authViewModel.loadCurrentUserInfo()
        }
    }
}