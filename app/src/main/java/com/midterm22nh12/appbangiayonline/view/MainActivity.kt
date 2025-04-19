package com.midterm22nh12.appbangiayonline.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ActivityMainBinding
import com.midterm22nh12.appbangiayonline.viewmodel.DepthPageTransformer
import com.midterm22nh12.appbangiayonline.viewmodel.MyViewpager2Adapter

class MainActivity : AppCompatActivity() {
    private lateinit var bindingMainActivity : ActivityMainBinding
    private lateinit var adapter: MyViewpager2Adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivity = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingMainActivity.root)
        setUpViewpager2()

//        setContentView(R.layout.create_account)
//        setContentView(R.layout.promotion)


    }
    //sự kiện chuyển đổi fragment
    private fun setUpViewpager2() {
        // Chọn menu System (ở giữa) ngay từ đầu
        bindingMainActivity.mainMenuBottomNavigation.selectedItemId = R.id.menu_home

        // Khởi tạo adapter
        adapter = MyViewpager2Adapter(this)
        bindingMainActivity.mainBodyViewPager2.adapter = adapter

        // Đặt ViewPager2 ở trang SystemFragment (index 1)
        bindingMainActivity.mainBodyViewPager2.currentItem = 1

        // Hiệu ứng chuyển đổi trang
        bindingMainActivity.mainBodyViewPager2.setPageTransformer(DepthPageTransformer())

        // Lắng nghe sự kiện thay đổi trang
        bindingMainActivity.mainBodyViewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bindingMainActivity.mainMenuBottomNavigation.selectedItemId = when (position) {
                    0 -> R.id.menu_shopping_cart
                    1 -> R.id.menu_home
                    2 -> R.id.menu_account
                    else -> R.id.menu_home
                }
            }
        })
        // Xử lý khi người dùng chọn một mục trên BottomNavigationView
        bindingMainActivity.mainMenuBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_shopping_cart -> bindingMainActivity.mainBodyViewPager2.currentItem = 0
                R.id.menu_home -> bindingMainActivity.mainBodyViewPager2.currentItem = 1
                R.id.menu_account -> bindingMainActivity.mainBodyViewPager2.currentItem = 2
            }
            true
        }
    }
}