package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.FirebaseDatabase
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewShoppingCartUser
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.databinding.AccountSetupUserBinding
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankShopUserBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.CartItem
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewShoppingCartUser
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.CartViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BlankFragmentShopUser : Fragment(),
    MyAdapterRecyclerViewShoppingCartUser.OnItemClickListener {

    private lateinit var bindingFragmentShopUser: FragmentBlankShopUserBinding
    private lateinit var cartViewModel: CartViewModel // Thay đổi thành lateinit
    private lateinit var orderViewModel : OrderViewModel
    private lateinit var authViewModel : AuthViewModel
    private lateinit var adapter: MyAdapterRecyclerViewShoppingCartUser
    private var userId: String = ""
    private var isInDeleteMode = false
    private var productsBeingOrdered: List<ItemRecyclerViewShoppingCartUser> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentShopUser = FragmentBlankShopUserBinding.inflate(inflater, container, false)
        bindingFragmentShopUser.viewFlipperShopUser.displayedChild = 0


        // Lấy userId từ Firebase Auth
        val firebaseAuth = FirebaseAuth.getInstance()
        userId = firebaseAuth.currentUser?.uid ?: ""
        Log.d("ShopFragment", "userId được lấy: $userId $")

        // Khởi tạo RecyclerView
        setupRecyclerView()

        // Thiết lập các nút chức năng
        setupButtons()

        // Sự kiện chuyển trang
        turnPageShoppingCartUser()

        return bindingFragmentShopUser.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Đảm bảo cartViewModel được khởi tạo khi view đã sẵn sàng
        cartViewModel = (activity as MainActivityUser).getSharedCartViewModel()
        orderViewModel = (activity as MainActivityUser).getSharedOrderViewModel()
        authViewModel = (activity as MainActivityUser).getSharedViewModel()

        // Thêm log kiểm tra
        Log.d("ShopFragment", "onViewCreated: Gọi loadCurrentUserInfo()")

        // Đảm bảo thông tin người dùng được tải
        authViewModel.loadCurrentUserInfo()

        // Theo dõi trạng thái tạo đơn hàng
        orderViewModel.createOrderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading(true)
                }
                is UiState.Success -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show()

                    // Sửa từ adapter.getSelectedItems() thành productsBeingOrdered
                        if (productsBeingOrdered.isNotEmpty()) {
                            Log.d("DEBUG_DELETE", "OBSERVER: Bắt đầu xóa ${productsBeingOrdered.size} sản phẩm")
                            for (item in productsBeingOrdered) {
                                Log.d("DEBUG_DELETE", "OBSERVER: Xóa sản phẩm: ${item.productName}, ${item.colorName}, ${item.size}")
                                cartViewModel.removeFromCart(
                                    userId = userId,
                                    productId = item.productId,
                                    colorName = item.colorName,
                                    size = item.size
                                )
                            }
                            Log.d("DEBUG_DELETE", "OBSERVER: Hoàn tất xóa sản phẩm")
                            productsBeingOrdered = emptyList()
                        }
                    (activity as? MainActivityUser)?.showConfirmationUser()
                }
                is UiState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_LONG).show()
                    // Đặt lại danh sách nếu có lỗi
                    productsBeingOrdered = emptyList()
                }
            }
        }

        // Chỉ observe và load sau khi ViewModel đã sẵn sàng
        observeCartState()
        loadCartItems()
    }

    //sự kiện hiển thị khuyển mãi
    private fun turnPageShoppingCartUser() {
        bindingFragmentShopUser.includeShoppingCartUser.llPromotionShoppingCartUser.setOnClickListener {
            bindingFragmentShopUser.viewFlipperShopUser.displayedChild = 1
        }
        bindingFragmentShopUser.includePromotionUser.ivBackPromotionUser.setOnClickListener {
            bindingFragmentShopUser.viewFlipperShopUser.displayedChild = 0
        }
        bindingFragmentShopUser.includeShoppingCartUser.ivBackShoppingCartUser.setOnClickListener {
            (activity as? MainActivityUser)?.returnToPreviousOverlay()
        }
    }
    private fun setupButtons() {
        // Nút chọn tất cả
        bindingFragmentShopUser.includeShoppingCartUser.cbAllShoppingCartUser.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAll(isChecked)
            updateSelectedItemsSummary()
        }

        // Nút xóa sản phẩm đã chọn
        bindingFragmentShopUser.includeShoppingCartUser.btDeleteShoppingCartUser.setOnClickListener {
            deleteSelectedItems()
        }

        // Nút Mua hàng
        bindingFragmentShopUser.includeShoppingCartUser.btByShoppingCartUser.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()

            if (selectedItems.isEmpty()) {
                Toast.makeText(context, "Vui lòng chọn sản phẩm để mua", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Chuyển đến màn hình thanh toán với các sản phẩm đã chọn
            showOrderConfirmationDialog(selectedItems)
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18", "SetTextI18n")
    private fun updateSelectedItemsSummary() {
        val selectedItems = adapter.getSelectedItems()
        val totalQuantity = selectedItems.sumOf { it.quantity }
        val totalPrice = selectedItems.sumOf { it.price * it.quantity }

        // Cập nhật nút Mua
        bindingFragmentShopUser.includeShoppingCartUser.btByShoppingCartUser.apply {
            text = "Mua( $totalQuantity )"
            isEnabled = selectedItems.isNotEmpty() // Vô hiệu hóa nút nếu không có sản phẩm nào được chọn
        }

        // Cập nhật tổng tiền
        bindingFragmentShopUser.includeShoppingCartUser.tvTotalPriceShoppingCartUser.text =
            String.format("%,d vnđ", totalPrice)
    }

    private fun deleteSelectedItems() {
        val selectedItems = adapter.getSelectedItems()
        if (selectedItems.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn sản phẩm để xóa", Toast.LENGTH_SHORT).show()
            return
        }

        // Xác nhận xóa
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa sản phẩm")
            .setMessage("Bạn có chắc chắn muốn xóa ${selectedItems.size} sản phẩm đã chọn?")
            .setPositiveButton("Xóa") { _, _ ->
                // Xóa từng sản phẩm đã chọn
                for (item in selectedItems) {
                    cartViewModel.removeFromCart(
                        userId = userId,
                        productId = item.productId,
                        colorName = item.colorName,
                        size = item.size
                    )
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = MyAdapterRecyclerViewShoppingCartUser(emptyList(), this)
        bindingFragmentShopUser.includeShoppingCartUser.rcShoppingCartUser.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@BlankFragmentShopUser.adapter
        }
        Log.d("ShopFragment", "RecyclerView đã được thiết lập")
    }

    private fun observeCartState() {
        cartViewModel.cartItemsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Hiển thị loading
                    Log.d("ShopFragment", "Đang tải giỏ hàng...")
                    bindingFragmentShopUser.includeShoppingCartUser.progressBarShoppingCartUser.visibility =
                        View.VISIBLE
                }

                is UiState.Success -> {
                    // Ẩn loading
                    bindingFragmentShopUser.includeShoppingCartUser.progressBarShoppingCartUser.visibility =
                        View.GONE

                    // Cập nhật RecyclerView
                    val cartItems = state.data.map { cartItem ->
                        ItemRecyclerViewShoppingCartUser(
                            id = "${cartItem.product.id}_${cartItem.selectedColor.name}_${cartItem.selectedSize}",
                            productId = cartItem.product.id,
                            productName = cartItem.product.name,
                            price = cartItem.product.price,
                            quantity = cartItem.quantity,
                            colorName = cartItem.selectedColor.name,
                            size = cartItem.selectedSize,
                            imageUrl = cartItem.selectedColor.image,
                            stock = cartItem.selectedColor.stock
                        )
                    }

                    Log.d("ShopFragment", "Nhận được ${cartItems.size} sản phẩm từ giỏ hàng")
                    cartItems.forEachIndexed { index, item ->
                        Log.d("ShopFragment", "Sản phẩm $index: ${item.productName}, màu: ${item.colorName}, size: ${item.size}")
                    }

                    adapter.updateData(cartItems)

                    // Cập nhật tổng tiền
                    updateCartSummary()
                }

                is UiState.Error -> {
                    // Ẩn loading
                    bindingFragmentShopUser.includeShoppingCartUser.progressBarShoppingCartUser.visibility =
                        View.GONE

                    // Hiển thị thông báo lỗi
                    Log.e("ShopFragment", "Lỗi khi tải giỏ hàng: ${state.message}")
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateCartSummary() {
        // Chỉ hiển thị thông tin tổng khi không có sản phẩm nào được chọn
        if (adapter.getSelectedItems().isEmpty()) {
            updateSelectedItemsSummary()
        }
    }

    private fun loadCartItems() {
        if (userId.isNotEmpty() && ::cartViewModel.isInitialized) {
            Log.d("ShopFragment", "Đang tải giỏ hàng cho userId: $userId")
            // Sử dụng phương thức realtime để có hiệu quả hơn
            cartViewModel.getCartItemsRealtime(userId)
        } else {
            Log.e("ShopFragment", "Không thể tải giỏ hàng: userId trống hoặc cartViewModel chưa được khởi tạo")
        }
    }

    override fun onDeleteClick(item: ItemRecyclerViewShoppingCartUser, position: Int) {
        // Xóa sản phẩm khỏi giỏ hàng
        cartViewModel.removeFromCart(
            userId = userId,
            productId = item.productId,
            colorName = item.colorName,
            size = item.size
        )
    }

    override fun onQuantityChange(
        item: ItemRecyclerViewShoppingCartUser,
        position: Int,
        newQuantity: Int
    ) {
        // Cập nhật số lượng sản phẩm
        cartViewModel.updateCartItemQuantity(
            userId = userId,
            productId = item.productId,
            colorName = item.colorName,
            size = item.size,
            quantity = newQuantity
        )
    }

    override fun onItemSelected(
        item: ItemRecyclerViewShoppingCartUser,
        position: Int,
        isSelected: Boolean
    ) {
        // Cập nhật tổng tiền và số lượng các sản phẩm đã chọn
        updateSelectedItemsSummary()

        // Kiểm tra và cập nhật checkbox "Chọn tất cả"
        val allSelected = adapter.getSelectedItems().size == adapter.itemCount
        bindingFragmentShopUser.includeShoppingCartUser.cbAllShoppingCartUser.isChecked = allSelected
    }

    override fun onResume() {
        super.onResume()
        // Tải lại dữ liệu giỏ hàng mỗi khi quay lại fragment
        if (::cartViewModel.isInitialized) {
            Log.d("ShopFragment", "Tải lại giỏ hàng trong onResume")
            cartViewModel.getCartItemsRealtime(userId)
        } else {
            Log.e("ShopFragment", "CartViewModel chưa được khởi tạo trong onResume")
            // Khởi tạo lại nếu cần
            if (activity != null) {
                cartViewModel = (activity as MainActivityUser).getSharedCartViewModel()
                observeCartState()
                loadCartItems()
            }
        }
    }
    // Phương thức hiển thị hộp thoại xác nhận
    @SuppressLint("DefaultLocale")
    private fun showOrderConfirmationDialog(selectedItems: List<ItemRecyclerViewShoppingCartUser>) {
        // Tạo hộp thoại
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Xác nhận đặt hàng")
       // Tính tổng tiền
        val totalAmount = selectedItems.sumOf { it.price * it.quantity }
        // Tạo thông tin đơn hàng
        val message = StringBuilder()
        message.append("Sản phẩm:\n")
        selectedItems.forEach { item ->
            message.append("- ${item.productName} (${item.colorName}, ${item.size}): ${item.quantity} x ${String.format("%,d", item.price)} vnđ\n")
        }
        message.append("\nTổng cộng: ${String.format("%,d",totalAmount)} vnđ")

        builder.setMessage(message.toString())

        // Nút xác nhận
        builder.setPositiveButton("Đặt hàng") { dialog, _ ->
            dialog.dismiss()
            processOrder(selectedItems)
        }

        // Nút hủy
        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()

        }

        // Hiển thị hộp thoại
        val dialog = builder.create()
        dialog.show()
    }

    // Phương thức xử lý đặt hàng
    private fun processOrder(selectedItems: List<ItemRecyclerViewShoppingCartUser>) {
        // Lưu danh sách sản phẩm đang đặt hàng để xử lý sau khi đặt hàng thành công
        productsBeingOrdered = selectedItems
        // Hiển thị loading
        showLoading(true)

        // Lấy thông tin người dùng
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isEmpty()) {
            showLoading(false)
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy CartViewModel để chuyển đổi dữ liệu
        val cartViewModel = (activity as? MainActivityUser)?.getSharedCartViewModel()
        if (cartViewModel == null) {
            showLoading(false)
            Toast.makeText(requireContext(), "Không thể xử lý đơn hàng, vui lòng thử lại", Toast.LENGTH_SHORT).show()
            return
        }
       // Thêm log khi đặt hàng
        Log.d("ShopFragment", "Đặt hàng ${selectedItems.size} sản phẩm đã chọn")
        // Lấy thông tin sản phẩm từ Firebase và tạo đơn hàng
        lifecycleScope.launch {
            try {
                // Khởi tạo Firebase Database nếu cần
                val database = FirebaseDatabase.getInstance()

                // Chuyển đổi từ ItemRecyclerViewShoppingCartUser sang CartItem
                val cartItems = mutableListOf<CartItem>()

                for (item in selectedItems) {
                    try {
                        // Lấy thông tin sản phẩm từ Firebase
                        val productRef = database.getReference("products")
                        val productSnapshot = productRef.child(item.productId).get().await()
                        val product = productSnapshot.getValue(Product::class.java)

                        if (product != null) {
                            // Tìm màu và size đã chọn
                            val selectedColor = product.colors.find { it.name == item.colorName }
                            val selectedSize = product.sizes.find { it.value == item.size }

                            if (selectedColor != null && selectedSize != null) {
                                // Tạo CartItem
                                val cartItem = CartItem(
                                    product = product,
                                    selectedColor = selectedColor,
                                    selectedSize = item.size,
                                    quantity = item.quantity
                                )
                                cartItems.add(cartItem)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ShopFragment", "Lỗi khi lấy thông tin sản phẩm: ${e.message}")
                    }
                }

                if (cartItems.isEmpty()) {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Không thể lấy thông tin sản phẩm, vui lòng thử lại",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                // Lấy thông tin người dùng hiện tại thay vì đăng ký observer mới
                val currentUser = authViewModel.currentUser.value
                if (currentUser == null || currentUser.address.isEmpty() || currentUser.phone.isEmpty()) {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        "Vui lòng cập nhật địa chỉ và số điện thoại trong hồ sơ",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                // Tạo đơn hàng với thông tin người dùng đã lấy được
                orderViewModel.createOrderFromCart(
                    userId = userId,
                    shippingAddress = currentUser.address,
                    phoneNumber = currentUser.phone,
                    paymentMethod = "COD",
                    note = "",
                    cartItems = cartItems
                )
            }catch (e: Exception){
                Log.e("ShopFragment", "Lỗi khi tạo đơn hàng: ${e.message}")
                showLoading(false)
                Toast.makeText(requireContext(), "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Hiển thị/ẩn loading
    private fun showLoading(show: Boolean) {
        bindingFragmentShopUser.includeShoppingCartUser.progressBarShoppingCartUser.visibility =
            if (show) View.VISIBLE else View.GONE
    }
}