package com.midterm22nh12.appbangiayonline.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.BrandHomeBinding
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankHomeBinding
import com.midterm22nh12.appbangiayonline.databinding.NotificationHomeBinding
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewBrandHome
import com.midterm22nh12.appbangiayonline.model.ItemRecyclerViewNotificationHome
import com.midterm22nh12.appbangiayonline.viewmodel.MyAdapterRecyclerViewBrandHome
import com.midterm22nh12.appbangiayonline.viewmodel.MyAdapterRecyclerViewNotificationHome

class BlankFragmentHome : Fragment() {
    private lateinit var bindingFragmentHome: FragmentBlankHomeBinding
    private lateinit var bindingNotificationHome : NotificationHomeBinding
    private lateinit var bindingBrandHome : BrandHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentHome= FragmentBlankHomeBinding.inflate(inflater,container,false)
        bindingNotificationHome= NotificationHomeBinding.bind(bindingFragmentHome.includeNotificationHome.notificationHome)
        bindingBrandHome=BrandHomeBinding.bind(bindingFragmentHome.includeBrandHome.brandHome)
        //  hiển thi thông báo
        showNotification()
        //hiển thị nhãn hàng
        showBrand()
        return bindingFragmentHome.root
    }
    //hiển thi thông báo
    private fun showNotification()
    {
        bindingNotificationHome.rcHomeNotification.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false )
        //tạo danh sách list ví dụ
        val list= listOf(
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes),
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes1),
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes2),
            ItemRecyclerViewNotificationHome("Sản phẩm mới ra mắt", "Giảm 50% cho mỗi sản phẩm", R.drawable.shoes3)
        )
        val adapter=MyAdapterRecyclerViewNotificationHome(list)
        bindingNotificationHome.rcHomeNotification.adapter=adapter
    }
    //hiển thị nhãn hàng
    private fun showBrand()
    {
        bindingBrandHome.rcBrandHome.layoutManager=LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false )
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
        bindingBrandHome.rcBrandHome.adapter=adapter
    }
}