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
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHome
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewNotificationHome
import com.midterm22nh12.appbangiayonline.viewmodel.MyAdapterRecyclerViewBrandHome
import com.midterm22nh12.appbangiayonline.viewmodel.MyAdapterRecyclerViewNotificationHome

class BlankFragmentHome : Fragment() {
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
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes),
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes1),
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes2),
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes3)
        )
        val adapter=MyAdapterRecyclerViewNotificationHome(list)
        bindingNotificationHomeUser.rcHomeNotification.adapter=adapter
    }
    //hiển thị nhãn hàng
    private fun showBrand()
    {
        bindingBrandHomeUser.rcBrandHome.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false )
        //tạo danh sách list ví dụ
        val list= listOf(
           ItemRecyclerViewBrandHome(R.drawable.cat1),
            ItemRecyclerViewBrandHome(R.drawable.cat2),
            ItemRecyclerViewBrandHome(R.drawable.cat3),
            ItemRecyclerViewBrandHome(R.drawable.cat4),
            ItemRecyclerViewBrandHome(R.drawable.cat5),
            ItemRecyclerViewBrandHome(R.drawable.cat6)
        )
        val adapter=MyAdapterRecyclerViewBrandHome(list)
        bindingBrandHomeUser.rcBrandHome.adapter=adapter
    }
}