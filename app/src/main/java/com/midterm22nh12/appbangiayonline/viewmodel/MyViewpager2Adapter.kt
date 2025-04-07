package com.midterm22nh12.appbangiayonline.viewmodel

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.midterm22nh12.appbangiayonline.view.BlankFragmentAccount
import com.midterm22nh12.appbangiayonline.view.BlankFragmentHome
import com.midterm22nh12.appbangiayonline.view.BlankFragmentShop

class MyViewpager2Adapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int =3

    override fun createFragment(position: Int): Fragment {
        return when (position)
        {
            0 -> BlankFragmentShop()
            1 -> BlankFragmentHome()
            2 -> BlankFragmentAccount()
            else -> BlankFragmentHome()
        }
    }
}