package com.midterm22nh12.appbangiayonline.Adapter.Admin

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.midterm22nh12.appbangiayonline.view.Admin.BlankFragmentBillAdmin
import com.midterm22nh12.appbangiayonline.view.Admin.BlankFragmentRevenueAdmin
import com.midterm22nh12.appbangiayonline.view.User.BlankFragmentAccountUser
import com.midterm22nh12.appbangiayonline.view.User.BlankFragmentHomeUser
import com.midterm22nh12.appbangiayonline.view.User.BlankFragmentShopUser

class MyViewpager2AdapterAdmin(activity: FragmentActivity) :
    FragmentStateAdapter(activity)
    {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> BlankFragmentBillAdmin()
                1 -> BlankFragmentHomeUser()
                2 -> BlankFragmentRevenueAdmin()
                else -> BlankFragmentRevenueAdmin()
            }
        }
    }