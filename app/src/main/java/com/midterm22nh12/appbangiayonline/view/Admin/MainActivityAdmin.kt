package com.midterm22nh12.appbangiayonline.view.Admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.midterm22nh12.appbangiayonline.Adapter.Admin.MyViewpager2AdapterAdmin
import com.midterm22nh12.appbangiayonline.Adapter.Message.ConversationAdapter
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.Ui.Animations.DepthPageTransformer
import com.midterm22nh12.appbangiayonline.Utils.ChatUtils
import com.midterm22nh12.appbangiayonline.databinding.ActivityMainAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Message.Conversation
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.view.Auth.LoginEndCreateAccount
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.Message.ChatViewModel
import java.util.ArrayDeque

class MainActivityAdmin : AppCompatActivity() {
    private lateinit var bindingMainActivityAdmin : ActivityMainAdminBinding
    private lateinit var adapter: MyViewpager2AdapterAdmin
    private lateinit var headerView: View
    private lateinit var drawerLayoutNotification: DrawerLayout
    private lateinit var navigationViewNotification: NavigationView
    lateinit var authViewModel: AuthViewModel
    lateinit var chatViewModel : ChatViewModel
    private val navigationHistory = ArrayDeque<Int>()//// Lưu trữ lịch sử overlay fragment
    private var currentPosition = 1
    private val overlayStack = ArrayDeque<String>() // Lưu trữ lịch sử overlay include

    private var listMessageHandler: list_messages_admin? = null
    private var messageHandler: messages_admin? = null  // Thêm biến này
    private var editEndAddProductHandler: edit_end_add_product? = null  // Thêm biến này
    private var currentViewingConversationId: String? = null // Thêm biến này
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMainActivityAdmin = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(bindingMainActivityAdmin.root)

        // khởi tạo drawerLayout và navigationView
        drawerLayoutNotification = bindingMainActivityAdmin.mainAdmin
        navigationViewNotification = bindingMainActivityAdmin.navigationViewNotificationAdmin
        //lấy header view từ navigationView
        headerView = navigationViewNotification.getHeaderView(0)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        ChatUtils.loadAdminList { success ->
            if (success) {
                Log.d("App--tt", "Danh sách admin đã được tải")
            } else {
                Log.e("App--tt", "Không thể tải danh sách admin")
            }
        }
        // chuyển trang
        setUpViewpager2()
        //đóng thông báo
        setUpNavigationView()
    }
    //ẩn menu
    fun hideBottomNav() {
        bindingMainActivityAdmin.mainMenuBottomNavigationAdmin.visibility=View.GONE
    }
    //hiện menu
    fun showBottomNav() {
        bindingMainActivityAdmin.mainMenuBottomNavigationAdmin.visibility = View.VISIBLE
    }
    //ẩn bàn phím
    fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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
    //sự kiện chuyển đổi fragment
    private fun setUpViewpager2() {
        // Chọn menu System (ở giữa) ngay từ đầu
        bindingMainActivityAdmin.mainMenuBottomNavigationAdmin.selectedItemId = R.id.menu_home_admin

        // Khởi tạo adapter
        adapter = MyViewpager2AdapterAdmin(this)
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
    // sự kiện quay về trang chủ
    fun returnPageUser() {
        // Nếu có vị trí trước đó trong lịch sử, quay lại vị trí đó
        if (navigationHistory.isNotEmpty()) {
            val previousPos = navigationHistory.pop()
            bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = previousPos
        } else {
            // Nếu không có vị trí trước đó, về home
            bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = 1
        }
    }
    //hàm chuyển đến trang fragment muốn đến
    fun navigateFromOverlayToFragment(index : Int) {
        saveCurrentOverlayToStack()

        hideAllOverlays()

        // Chuyển ViewPager2 đến vị trí của fragment giỏ hàng (index 0)
        bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem = index
        currentPosition = index
        // Lưu vị trí hiện tại vào lịch sử
        navigationHistory.push(currentPosition)
    }
    // Hàm để quay lại overlay trước đó hoặc về trang gốc
    // Cập nhật phương thức returnToPreviousOverlay
    fun returnToPreviousOverlay() {
        hideAllOverlays()

        // Kiểm tra xem có overlay trước đó không
        if (overlayStack.isNotEmpty()) {
            // Lấy tên overlay trước đó từ stack
            val previousOverlay = overlayStack.pop()
            Log.d("OverlayDebug", "Pop overlay: $previousOverlay")

            // Hiển thị overlay tương ứng
            when (previousOverlay) {
                "listMessages" -> {
                    Log.d("OverlayDebug", "Hiển thị listMessages")
                    bindingMainActivityAdmin.listMessagesActivityMainAdmin.root.visibility = View.VISIBLE

                    // Nếu có ID cuộc hội thoại đang xem, truyền cho list_messages_admin
                    if (currentViewingConversationId != null) {
                        listMessageHandler?.setLastViewedConversationId(currentViewingConversationId)
                        // Reset sau khi đã sử dụng
                        currentViewingConversationId = null
                    }

                    // Sử dụng refreshConversations thay vì loadConversations
                    listMessageHandler?.refreshConversations()
                }
                "message" -> {
                    Log.d("OverlayDebug", "Hiển thị message")
                    bindingMainActivityAdmin.messagesActivityMainAdmin.root.visibility = View.VISIBLE
                }
                "editEndAddProduct" -> {
                    Log.d("OverlayDebug", "Hiển thị editEndAddProduct")
                    bindingMainActivityAdmin.editEndAddProductActivityMainAdmin.root.visibility = View.VISIBLE
                }
                // Thêm các overlay khác nếu cần
            }
        } else {
            Log.d("OverlayDebug", "Stack rỗng, quay về fragment")
            // Nếu không còn overlay nào trong stack, quay về fragment
            returnPageUser()
        }
    }
    // Hàm để ẩn tất cả các overlay
    private fun hideAllOverlays() {
        bindingMainActivityAdmin.listMessagesActivityMainAdmin.root.visibility = View.GONE
        bindingMainActivityAdmin.messagesActivityMainAdmin.root.visibility = View.GONE
        bindingMainActivityAdmin.editEndAddProductActivityMainAdmin.root.visibility = View.GONE
        // Ẩn các overlay khác nếu có
    }
    // Hàm để lưu overlay hiện tại vào stack
    private fun saveCurrentOverlayToStack() {
        when {
            bindingMainActivityAdmin.listMessagesActivityMainAdmin.root.visibility == View.VISIBLE ->
                overlayStack.push("listMessages")
            bindingMainActivityAdmin.messagesActivityMainAdmin.root.visibility == View.VISIBLE ->
                overlayStack.push("message")
            bindingMainActivityAdmin.editEndAddProductActivityMainAdmin.root.visibility == View.VISIBLE ->
                overlayStack.push("editEndAddProduct")
            // Thêm các overlay khác nếu cần
        }
    }
    // Cập nhật lại hàm showOrderUser
    fun showListMessageAdmin() {
        saveCurrentOverlayToStack()
        navigationHistory.push(bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem)
        hideAllOverlays()

        // Hiển thị overlay danh sách tin nhắn
        bindingMainActivityAdmin.listMessagesActivityMainAdmin.root.visibility = View.VISIBLE

        // Tạo handler nếu chưa tồn tại hoặc gọi lại loadConversations() nếu đã tồn tại
        if (listMessageHandler == null) {
            val listMessageBinding = bindingMainActivityAdmin.listMessagesActivityMainAdmin
            listMessageHandler = list_messages_admin(this, listMessageBinding, this)
        } else {
            // Chỉ cần gọi lại hàm load để tải lại dữ liệu
            listMessageHandler?.loadConversations()
        }
    }
    // Cập nhật phương thức showMessageAdmin
    fun showMessageAdmin(conversation: Conversation) {
        saveCurrentOverlayToStack()
        navigationHistory.push(bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem)
        hideAllOverlays()

        // Lưu ID cuộc hội thoại đang xem
        currentViewingConversationId = conversation.id

        // Hiển thị overlay chat
        bindingMainActivityAdmin.messagesActivityMainAdmin.root.visibility = View.VISIBLE

        // Khởi tạo handler và lưu tham chiếu
        val messageBinding = bindingMainActivityAdmin.messagesActivityMainAdmin
        messageHandler = messages_admin(this, messageBinding, conversation, this)
    }
    //cập nhật phương thức showEditEndAddProduct
    fun showEditEndAddProduct(product : ItemRecyclerViewProductHomeUser) {
        saveCurrentOverlayToStack()
        navigationHistory.push(bindingMainActivityAdmin.mainBodyViewPager2Admin.currentItem)
        hideAllOverlays()
        bindingMainActivityAdmin.editEndAddProductActivityMainAdmin.root.visibility = View.VISIBLE
        // Khởi tạo handler và lưu tham chiếu
        val editEndAddProductBinding = bindingMainActivityAdmin.editEndAddProductActivityMainAdmin
        editEndAddProductHandler = edit_end_add_product(this, editEndAddProductBinding, product, this)
    }
}