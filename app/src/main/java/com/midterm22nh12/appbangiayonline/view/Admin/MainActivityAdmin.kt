package com.midterm22nh12.appbangiayonline.view.Admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.midterm22nh12.appbangiayonline.Adapter.User.MyViewpager2Adapter
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.Ui.Animations.DepthPageTransformer
import com.midterm22nh12.appbangiayonline.databinding.ActivityMainAdminBinding

class MainActivityAdmin : AppCompatActivity() {
    private lateinit var bindingMainActivityAdmin : ActivityMainAdminBinding
    private lateinit var adapter: MyViewpager2Adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivityAdmin = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(bindingMainActivityAdmin.root)

        // chuyển trang
        setUpViewpager2()
    }

    //sự kiện chuyển đổi fragment
    private fun setUpViewpager2() {
        // Chọn menu System (ở giữa) ngay từ đầu
        bindingMainActivityAdmin.mainMenuBottomNavigationAdmin.selectedItemId = R.id.menu_home_admin

        // Khởi tạo adapter
        adapter = MyViewpager2Adapter(this)
        bindingMainActivityAdmin.mainBodyViewPager2Admin.adapter = adapter

        // Đặt ViewPager2 ở trang SystemFragment (index 1)
        bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = 1

        // Hiệu ứng chuyển đổi trang
        bindingMainActivityAdmin.mainBodyViewPager2Admin.setPageTransformer(DepthPageTransformer())

        // Lắng nghe sự kiện thay đổi trang
        bindingMainActivityAdmin.mainBodyViewPager2Admin.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bindingMainActivityAdmin.mainMenuBottomNavigationAdmin.selectedItemId =
                    when (position) {
                        0 -> R.id.menu_shopping_cart_admin
                        1 -> R.id.menu_home_admin
                        2 -> R.id.menu_revenue_admin
                        else -> R.id.menu_home_admin
                    }
            }
        })
        // Xử lý khi người dùng chọn một mục trên BottomNavigationView
        bindingMainActivityAdmin.mainMenuBottomNavigationAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_shopping_cart_admin -> bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = 0
                R.id.menu_home_admin -> bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = 1
                R.id.menu_revenue_admin -> bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = 2
            }
            true
        }
    }
}