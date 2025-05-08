package com.midterm22nh12.appbangiayonline.view.User

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankAccountUserBinding
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
        return bindingFragmentBlankAccountUser.root
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
        if (authViewModel.isUserLoggedIn() && authViewModel.userName.value == null) {
            authViewModel.loadUserName()
        }
    }
}