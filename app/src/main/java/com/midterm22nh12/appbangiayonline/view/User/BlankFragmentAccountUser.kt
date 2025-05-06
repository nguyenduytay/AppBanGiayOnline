package com.midterm22nh12.appbangiayonline.view.User

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankAccountUserBinding

class BlankFragmentAccountUser : Fragment() {
    private lateinit var bindingFragmentBlankAccountUser: FragmentBlankAccountUserBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentBlankAccountUser= FragmentBlankAccountUserBinding.inflate(inflater, container, false)

        //hiển thị màn hình tài khoản đầu tiên
        bindingFragmentBlankAccountUser.viewFlipperAccountUser.displayedChild = 0
        //sự kiện chuyển trang
        turnPageAccountUser()
        //sự kiện đăng xuất
        logout()
        return bindingFragmentBlankAccountUser.root
    }

    //sự kiên hiển thị mục cài đặt
    private fun turnPageAccountUser()
    {
        bindingFragmentBlankAccountUser.includeAccountUser.ivSettingAccountUser.setOnClickListener{
            bindingFragmentBlankAccountUser.viewFlipperAccountUser.displayedChild = 1
        }
        bindingFragmentBlankAccountUser.includeAccountStepUser.ivBackAccountSetupUser.setOnClickListener{
            bindingFragmentBlankAccountUser.viewFlipperAccountUser.displayedChild = 0
        }
        bindingFragmentBlankAccountUser.includeAccountUser.ivBackAccountUser.setOnClickListener{
            (activity as? MainActivityUser)?.returnHomeUser()
        }
    }
    //sự kiện đăng xuất tài khoản
    private fun logout()
    {
        bindingFragmentBlankAccountUser.includeAccountStepUser.btLogoutAccountSetupUser.setOnClickListener{
            (activity as? MainActivityUser)?.logout()
        }
    }
}