package com.midterm22nh12.appbangiayonline.view.User

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewNotificationUser
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ActivityMainUserBinding
import com.midterm22nh12.appbangiayonline.Ui.Animations.DepthPageTransformer
import com.midterm22nh12.appbangiayonline.Adapter.User.MyViewpager2Adapter
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewNotificationUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.view.Auth.LoginEndCreateAccount
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
import java.util.ArrayDeque

class MainActivityUser : AppCompatActivity() {
    private lateinit var bindingMainActivityUser: ActivityMainUserBinding
    private lateinit var adapter: MyViewpager2Adapter
    private lateinit var drawerLayoutNotification: DrawerLayout
    private lateinit var navigationViewNotification: NavigationView
    private lateinit var headerView: View
    private lateinit var authViewModel: AuthViewModel
    private val navigationHistory = ArrayDeque<Int>()//// Lưu trữ lịch sử overlay fragment
    private var currentPosition = 1
    private val overlayStack = ArrayDeque<String>() // Lưu trữ lịch sử overlay include

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivityUser = ActivityMainUserBinding.inflate(layoutInflater)
        setContentView(bindingMainActivityUser.root)

        // khởi tạo drawerLayout và navigationView
        drawerLayoutNotification = bindingMainActivityUser.mainUser
        navigationViewNotification = bindingMainActivityUser.navigationViewNotificationUser
        //lấy header view từ navigationView
        headerView = navigationViewNotification.getHeaderView(0)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        //thiết lập sự kiện cho nút back trong header của navigationView
        setUpNavigationView()
        // chuyển trang
        setUpViewpager2()

    }

    //sự kiện chuyển đổi fragment
    private fun setUpViewpager2() {
        // Chọn menu System (ở giữa) ngay từ đầu
        bindingMainActivityUser.mainMenuBottomNavigationUser.selectedItemId = R.id.menu_home_user

        // Khởi tạo adapter
        adapter = MyViewpager2Adapter(this)
        bindingMainActivityUser.mainBodyViewPager2User.adapter = adapter

        // Đặt ViewPager2 ở trang SystemFragment (index 1)
        bindingMainActivityUser.mainBodyViewPager2User.currentItem = 1

        // Hiệu ứng chuyển đổi trang
        bindingMainActivityUser.mainBodyViewPager2User.setPageTransformer(DepthPageTransformer())

        // Lắng nghe sự kiện thay đổi trang
        bindingMainActivityUser.mainBodyViewPager2User.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Nếu vị trí thay đổi, lưu vị trí trước đó vào lịch sử
                if (position != currentPosition) {
                    navigationHistory.push(currentPosition)
                    currentPosition = position
                }
                // Ẩn/hiện menu dựa vào Fragment
                when (position) {
                    0 -> hideBottomNav() // Shopping cart - ẩn menu
                    1 -> showBottomNav() // Home - hiện menu
                    2 -> hideBottomNav() // Account - ẩn menu
                }
                bindingMainActivityUser.mainMenuBottomNavigationUser.selectedItemId =
                    when (position) {
                        0 -> R.id.menu_shopping_cart_user
                        1 -> R.id.menu_home_user
                        2 -> R.id.menu_account_user
                        else -> R.id.menu_home_user
                    }
            }
        })
        // Xử lý khi người dùng chọn một mục trên BottomNavigationView
        bindingMainActivityUser.mainMenuBottomNavigationUser.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_shopping_cart_user -> bindingMainActivityUser.mainBodyViewPager2User.currentItem = 0
                R.id.menu_home_user -> bindingMainActivityUser.mainBodyViewPager2User.currentItem = 1
                R.id.menu_account_user -> bindingMainActivityUser.mainBodyViewPager2User.currentItem = 2
            }
            true
        }
    }

    //thiết lập navigation
    private fun setUpNavigationView() {
        // tìm nút back trong header
        val backButton = headerView.findViewById<ImageView>(R.id.iv_back_notification_home_user)
        // thiết lập sự kiện click
        backButton?.setOnClickListener {
            closeNotificationDrawer()
        }

    }

    // Phương thức để Fragment gọi khi cần mở NavigationDrawer
    fun openNotificationDrawer() {
        if (!drawerLayoutNotification.isDrawerOpen(GravityCompat.END)) {
            drawerLayoutNotification.openDrawer(GravityCompat.END)
        }
    }

    // Phương thức để Fragment gọi khi cần đóng NavigationDrawer
    private fun closeNotificationDrawer() {
        if (drawerLayoutNotification.isDrawerOpen(GravityCompat.END)) {
            drawerLayoutNotification.closeDrawer(GravityCompat.END)
        }
    }

    // Kiểm tra trạng thái drawer
    fun isDrawerOpen(): Boolean {
        return drawerLayoutNotification.isDrawerOpen(GravityCompat.END)
    }

    //hàm thêm sự kiện vào thông báo
    fun setupItemNotification() {
        // khởi tạo recyclerView
        val recyclerViewNotificationUser =
            headerView.findViewById<RecyclerView>(R.id.rc_notification_user)
        recyclerViewNotificationUser.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val list = listOf(
            ItemRecyclerViewNotificationUser("Tin nhắn", "Chủ shop: cần hỗ trợ gì"),
            ItemRecyclerViewNotificationUser("Sản phẩm", "Sản phẩm mới ra mắt"),
            ItemRecyclerViewNotificationUser("Đơn hàng", "Đơn hàng dã được giao"),
            ItemRecyclerViewNotificationUser("Gmail", "Đã gửi OTP về gmail của bạn"),
            ItemRecyclerViewNotificationUser("Tài khoản", "Tài khoản bạn đã đổi mật khẩu")
        )
        val adapter = MyAdapterRecyclerViewNotificationUser(list)
        recyclerViewNotificationUser.adapter = adapter
    }
    //ẩn menu
    fun hideBottomNav() {
        bindingMainActivityUser.mainMenuBottomNavigationUser.visibility=View.GONE
    }
    //hiện menu
    fun showBottomNav() {
        bindingMainActivityUser.mainMenuBottomNavigationUser.visibility = View.VISIBLE
    }
    // sự kiện quay về trang chủ
    fun returnPageUser() {
        // Nếu có vị trí trước đó trong lịch sử, quay lại vị trí đó
        if (navigationHistory.isNotEmpty()) {
            val previousPos = navigationHistory.pop()
            bindingMainActivityUser.mainBodyViewPager2User.currentItem = previousPos
        } else {
            // Nếu không có vị trí trước đó, về home
            bindingMainActivityUser.mainBodyViewPager2User.currentItem = 1
        }
    }
    //sự kiện đăng xuất
    fun logout() {
        authViewModel.logoutUser()
        //thiết lập quan sát kết quả
        authViewModel.logoutResult.observe(this)
        { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = {
                        val intent = Intent(this, LoginEndCreateAccount::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        //đóng activity hiện tại
                        finish()
                    },
                    onFailure = {
                        Toast.makeText(this,"Đăng xuất thất bại", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
    //hàm chuyển đến trang fragment muốn đến
    fun navigateFromOverlayToFragment(index : Int) {
        saveCurrentOverlayToStack()

        hideAllOverlays()

        // Chuyển ViewPager2 đến vị trí của fragment giỏ hàng (index 0)
        bindingMainActivityUser.mainBodyViewPager2User.currentItem = index
        currentPosition = index
        // Lưu vị trí hiện tại vào lịch sử
        navigationHistory.push(currentPosition)
    }
    // Hàm để quay lại overlay trước đó hoặc về trang gốc
    fun returnToPreviousOverlay() {
        // Ẩn tất cả overlay hiện tại
        hideAllOverlays()

        // Kiểm tra xem có overlay trước đó không
        if (overlayStack.isNotEmpty()) {
            // Lấy tên overlay trước đó từ stack
            val previousOverlay = overlayStack.pop()

            // Hiển thị overlay tương ứng
            when (previousOverlay) {
                "messages" -> bindingMainActivityUser.messagesOverlayActivityMainUser.root.visibility = View.VISIBLE
                "order" -> bindingMainActivityUser.orderUserActivityMainUser.root.visibility = View.VISIBLE
                "rating" -> bindingMainActivityUser.ratingUserActivityMainUser.root.visibility = View.VISIBLE
                // Thêm các overlay khác nếu cần
            }
        } else {
            // Nếu không còn overlay nào trong stack, quay về fragment
            returnPageUser()
        }
    }
    // Hàm để ẩn tất cả các overlay
    private fun hideAllOverlays() {
        bindingMainActivityUser.messagesOverlayActivityMainUser.root.visibility = View.GONE
        bindingMainActivityUser.orderUserActivityMainUser.root.visibility = View.GONE
        bindingMainActivityUser.ratingUserActivityMainUser.root.visibility = View.GONE
        // Ẩn các overlay khác nếu có
    }
    // Cập nhật lại hàm showMessagesOverlay
    fun showMessagesOverlay(item : ItemRecyclerViewProductHomeUser?=null) {
        // Lưu overlay hiện tại vào stack (nếu có)
        saveCurrentOverlayToStack()

        navigationHistory.push( bindingMainActivityUser.mainBodyViewPager2User.currentItem)

        // Ẩn tất cả overlay
        hideAllOverlays()

        // Hiển thị overlay tin nhắn
        bindingMainActivityUser.messagesOverlayActivityMainUser.root.visibility = View.VISIBLE

        // Khởi tạo handler
        val messagesBinding = bindingMainActivityUser.messagesOverlayActivityMainUser
        val messagesHandler = messages_user(this, messagesBinding,item)
    }

    // Cập nhật lại hàm showOrderUser
    fun showOrderUser(item: ItemRecyclerViewProductHomeUser) {
        // Lưu overlay hiện tại vào stack (nếu có)
        saveCurrentOverlayToStack()

        navigationHistory.push( bindingMainActivityUser.mainBodyViewPager2User.currentItem)

        // Ẩn tất cả overlay
        hideAllOverlays()

        // Hiển thị overlay đặt hàng
        bindingMainActivityUser.orderUserActivityMainUser.root.visibility = View.VISIBLE

        // Khởi tạo handler
        val orderBinding = bindingMainActivityUser.orderUserActivityMainUser
        val orderHandler = order_user(this, orderBinding, item)
    }

    // Thêm hàm mới hiển thị overlay đánh giá
    fun showRatingUser() {
        // Lưu overlay hiện tại vào stack (nếu có)
        saveCurrentOverlayToStack()

        // Ẩn tất cả overlay
        hideAllOverlays()

        // Hiển thị overlay đánh giá
        bindingMainActivityUser.ratingUserActivityMainUser.root.visibility = View.VISIBLE

        // Khởi tạo handler
        val ratingBinding = bindingMainActivityUser.ratingUserActivityMainUser
        val ratingHandler = rating_user(this, ratingBinding)
    }

    // Hàm để lưu overlay hiện tại vào stack
    private fun saveCurrentOverlayToStack() {
        when {
            bindingMainActivityUser.messagesOverlayActivityMainUser.root.visibility == View.VISIBLE ->
                overlayStack.push("messages")
            bindingMainActivityUser.orderUserActivityMainUser.root.visibility == View.VISIBLE ->
                overlayStack.push("order")
            bindingMainActivityUser.ratingUserActivityMainUser.root.visibility == View.VISIBLE ->
                overlayStack.push("rating")
            // Thêm các overlay khác nếu cần
        }
    }
}