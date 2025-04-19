package com.midterm22nh12.appbangiayonline.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.BrandHomeUserBinding
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankHomeUserBinding
import com.midterm22nh12.appbangiayonline.databinding.NotificationHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHomeUser
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewNotificationHomeUser
import com.midterm22nh12.appbangiayonline.viewmodel.MyAdapterRecyclerViewBrandHomeUser
import com.midterm22nh12.appbangiayonline.viewmodel.MyAdapterRecyclerViewNotificationHomeUser

class BlankFragmentHomeUser : Fragment() {
    private lateinit var bindingFragmentHomeUser: FragmentBlankHomeUserBinding
    private lateinit var bindingNotificationHomeUser : NotificationHomeUserBinding
    private lateinit var bindingBrandHomeUser : BrandHomeUserBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentHomeUser= FragmentBlankHomeUserBinding.inflate(inflater,container,false)
        bindingNotificationHomeUser= NotificationHomeUserBinding.bind(bindingFragmentHomeUser.includeNotificationHome.notificationHome)
        bindingBrandHomeUser=BrandHomeUserBinding.bind(bindingFragmentHomeUser.includeBrandHome.brandHome)
        //  hiển thi thông báo
        showNotification()
        //hiển thị nhãn hàng
        showBrand()
        return bindingFragmentHomeUser.root
    }
    //hiển thi thông báo
    private fun showNotification()
    {
        bindingNotificationHomeUser.rcHomeNotification.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false )
        //tạo danh sách list ví dụ
        val list= listOf(
            ItemRecyclerViewNotificationHomeUser("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes),
            ItemRecyclerViewNotificationHomeUser("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes1),
            ItemRecyclerViewNotificationHomeUser("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes2),
            ItemRecyclerViewNotificationHomeUser("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes3)
        )
        val adapter=MyAdapterRecyclerViewNotificationHomeUser(list)
        bindingNotificationHomeUser.rcHomeNotification.adapter=adapter
    }
    //hiển thị nhãn hàng
    private fun showBrand()
    {
        bindingBrandHomeUser.rcBrandHome.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false )
        //tạo danh sách list ví dụ
        val list= listOf(
            ItemRecyclerViewBrandHomeUser(R.drawable.cat1),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat2),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat3),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat4),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat5),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat6)
        )
        val adapter=MyAdapterRecyclerViewBrandHomeUser(list)
        bindingBrandHomeUser.rcBrandHome.adapter=adapter
    }
}