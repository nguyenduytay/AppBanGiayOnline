package com.midterm22nh12.appbangiayonline.view.Admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewBrandHomeUser
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewTypeProductHomeUser
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankHomeAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Category
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewBrandHomeUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewTypeProductHomeUser
import com.midterm22nh12.appbangiayonline.view.User.MainActivityUser
import com.midterm22nh12.appbangiayonline.viewmodel.BrandViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.CategoryViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.Message.ChatViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.ProductViewModel

class BlankFragmentHomeAdmin : Fragment() {

    private lateinit var binding: FragmentBlankHomeAdminBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var brandViewModel: BrandViewModel
    private var brandsList = listOf<Brand>()

    private lateinit var categoryViewModel: CategoryViewModel
    private var categoriesList = listOf<Category>()

    private lateinit var productViewModel : ProductViewModel
    private var productList = listOf<Product>()

    // Biến lưu trữ tiêu chí tìm kiếm hiện tại
    private var currentCategoryId: String? = null
    private var currentBrandId: String? = null
    private var currentSearchQuery: String? = null
    private val TAG = "BlankFragmentHomeAdmin"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBlankHomeAdminBinding.inflate(inflater, container, false)

        // Lấy ViewModel từ Activity để đảm bảo dùng chung instance
        chatViewModel = ViewModelProvider(requireActivity())[ChatViewModel::class.java]

        // Khởi tạo ViewModel
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        brandViewModel = ViewModelProvider(this)[BrandViewModel::class.java]
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        //hiển thị nhãn hàng
        showBrand()
        // sự kiện lựa chọn sản phẩm theo loại
        showTypeProduct()
        //hiển thị danh sách giày
        showProduct()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập click listeners
        setupClickIconView()

        setupUnreadCountListener()

    }

    private fun setupClickIconView() {
        binding.ivNotificationHomeAdmin.setOnClickListener {
            //(activity as MainActivityAdmin).openNotificationDrawer()

        }

        binding.ivMessageHomeAdmin.setOnClickListener {
            (activity as MainActivityAdmin).showListMessageAdmin()
        }

        //cho full kích thước chiều ngang
        binding.svSearchHomeAdmin.maxWidth = Int.MAX_VALUE
        binding.svSearchHomeAdmin.setOnSearchClickListener {
            // Xử lý sự kiện tìm kiếm
            binding.llHelloHomeAdmin.visibility = View.GONE
            binding.ivNotificationHomeAdmin.visibility = View.GONE
            binding.ivMessageHomeAdmin.visibility = View.GONE
            (activity as MainActivityAdmin).hideBottomNav()
        }
        binding.svSearchHomeAdmin.setOnCloseListener {
            binding.llHelloHomeAdmin.visibility = View.VISIBLE
            binding.ivNotificationHomeAdmin.visibility = View.VISIBLE
            binding.ivMessageHomeAdmin.visibility = View.VISIBLE
            resetAllSearchCriteria()
            (activity as MainActivityAdmin).showBottomNav()
            (activity as MainActivityAdmin).hideKeyboard(binding.svSearchHomeAdmin)
            false
        }
        //sự kiện thêm sản phẩm mới
        binding.btAddProductHomeAdmin.setOnClickListener {
            (activity as MainActivityAdmin).showEditEndAddProduct()
        }
        // Xử lý sự kiện tìm kiếm
        binding.svSearchHomeAdmin.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Thực hiện tìm kiếm khi nhấn Enter
                currentSearchQuery = query
                performSearch(currentCategoryId, currentBrandId, currentSearchQuery)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Tùy chọn: Thực hiện tìm kiếm theo thời gian thực
                if ((newText?.length ?: 0) >= 2 || newText.isNullOrEmpty()) {
                    currentSearchQuery = newText
                    performSearch(currentCategoryId, currentBrandId, currentSearchQuery)
                }
                return true
            }
        })
        // Thiết lập sự kiện click cho loại sản phẩm
        val typeProductAdapter = binding.includeTypeProductHome.rcTypeProductHome.adapter as? MyAdapterRecyclerViewTypeProductHomeUser
        typeProductAdapter?.setOnItemClickListener(object : MyAdapterRecyclerViewTypeProductHomeUser.OnItemClickListener {
            override fun onItemClick(item: ItemRecyclerViewTypeProductHomeUser, position: Int) {
                // Kiểm tra nếu là "Tất cả" (vị trí 0) thì đặt categoryId là null
                currentCategoryId = if (position == 0) null else {
                    // Lấy ID của category dựa vào vị trí (trừ 1 vì đã thêm "Tất cả" vào đầu)
                    if (position - 1 < categoriesList.size) categoriesList[position - 1].id else null
                }

                // Cập nhật UI để hiển thị chọn
                typeProductAdapter.updateSelectedPosition(position)

                // Thực hiện tìm kiếm với tiêu chí mới
                performSearch(currentCategoryId, currentBrandId, currentSearchQuery)
            }
        })
        // Thiết lập sự kiện click cho thương hiệu
        val brandAdapter = binding.includeBrandHome.rcBrandHome.adapter as? MyAdapterRecyclerViewBrandHomeUser
        brandAdapter?.setOnItemClickListener(object : MyAdapterRecyclerViewBrandHomeUser.OnItemClickListener {
            override fun onItemClick(item: ItemRecyclerViewBrandHomeUser, position: Int) {
                // Kiểm tra nếu là "Tất cả" (vị trí 0) thì đặt brandId là null
                currentBrandId = if (position == 0) null else {
                    // Lấy ID của brand dựa vào vị trí (trừ 1 vì đã thêm "Tất cả" vào đầu)
                    if (position - 1 < brandsList.size) brandsList[position - 1].id else null
                }

                // Cập nhật UI để hiển thị chọn
                brandAdapter.updateSelectedPosition(position)

                // Thực hiện tìm kiếm với tiêu chí mới
                performSearch(currentCategoryId, currentBrandId, currentSearchQuery)
            }
        })
    }
    private fun setupUnreadCountListener() {
        // Bắt đầu lắng nghe trước khi thiết lập observer
        Log.d(TAG, "Bắt đầu lắng nghe số tin nhắn chưa đọc")
        chatViewModel.startListeningToUnreadCount()

        // Thiết lập observer để cập nhật UI khi có thay đổi
        chatViewModel.unreadConversationsCount.observe(viewLifecycleOwner) { count ->
            Log.d(TAG, "Nhận cập nhật số tin nhắn chưa đọc: $count")
            updateBadgeUI(count)
        }
    }

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun updateBadgeUI(count: Int) {
        Log.e(TAG, "Badge TextView không tồn tại ${count}")
        binding.ivMessageHomeAdmin.visibility = View.VISIBLE
            if (count > 0) {
                if(count>9)
                    binding.ivMessageHomeAdmin.text = "9+"
                else
                    binding.ivMessageHomeAdmin.text = count.toString()
            } else {
                binding.ivMessageHomeAdmin.text = ""
            }
    }
    //hiển thị nhãn hàng
    private fun showBrand() {
        binding.includeBrandHome.rcBrandHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = MyAdapterRecyclerViewBrandHomeUser(emptyList(),
            object : MyAdapterRecyclerViewBrandHomeUser.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewBrandHomeUser, position: Int) {
                }
            })
        binding.includeBrandHome.rcBrandHome.adapter = adapter
        // Quan sát dữ liệu từ ViewModel
        brandViewModel.getBrands().observe(viewLifecycleOwner) { brands ->
            brandsList = brands
            // Tạo danh sách có thể thay đổi
            val items = mutableListOf<ItemRecyclerViewBrandHomeUser>()

            // Thêm item "Tất cả" vào đầu danh sách
            items.add(ItemRecyclerViewBrandHomeUser(R.drawable.all_foreground, "Tất cả"))

            // Thêm các brand từ danh sách brands
            items.addAll(brands.map { brand ->
                ItemRecyclerViewBrandHomeUser(
                    image = brand.image,
                    name = brand.name
                )
            })
            adapter.updateData(items)
        }
    }
    // showProduct()
    private fun showProduct() {
        binding.includeProductHome.rcProductHome.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)

        // Tạo adapter với xử lý sự kiện click
        val adapter = MyAdapterRecyclerViewProductHomeUser(emptyList(),
            object : MyAdapterRecyclerViewProductHomeUser.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewProductHomeUser, position: Int) {
                    (activity as MainActivityAdmin).showEditEndAddProduct(item)
                }
                override fun onFavoriteClick(item: ItemRecyclerViewProductHomeUser, position: Int) {
                    // Xử lý sự kiện favorite click
                }
            }
        )
        binding.includeProductHome.rcProductHome.adapter = adapter

        // Observe products với xử lý lỗi
        try {
            productViewModel.getProducts().observe(viewLifecycleOwner) { products ->
                try {
                    productList = products

                    val items = products.mapNotNull { product ->
                        try {
                            ItemRecyclerViewProductHomeUser(
                                id = product.id,
                                brandId = product.brandId,
                                categoryId = product.categoryId,
                                name = product.name,
                                price = product.price,
                                rating = product.rating,
                                description = product.description,
                                sizes = product.sizes,
                                colors = product.colors
                            )
                        } catch (e: Exception) {
                            Log.e("ProductHomeUser", "Error mapping product: ${e.message}")
                            null
                        }
                    }

                    adapter.updateData(items)
                } catch (e: Exception) {
                    Log.e("ProductHomeUser", "Error updating product list: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ProductHomeUser", "Error observing products: ${e.message}")
        }
    }
    // sự kiện lựa chọn sản phẩm theo loại
    private fun showTypeProduct() {
        binding.includeTypeProductHome.rcTypeProductHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = MyAdapterRecyclerViewTypeProductHomeUser(emptyList(),
            object : MyAdapterRecyclerViewTypeProductHomeUser.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewTypeProductHomeUser, position: Int) {
                }
            })
        binding.includeTypeProductHome.rcTypeProductHome.adapter = adapter
        categoryViewModel.getCategories().observe(viewLifecycleOwner) { categories ->
            categoriesList = categories

            val items=  mutableListOf<ItemRecyclerViewTypeProductHomeUser>()

            items.add(ItemRecyclerViewTypeProductHomeUser(name = "Tất cả"))

            items.addAll(categories.map { category ->
                ItemRecyclerViewTypeProductHomeUser(
                    name = category.name
                )
            })
            adapter.updateData(items)
        }

    }
    // Phương thức reset tất cả tiêu chí tìm kiếm và UI
    private fun resetAllSearchCriteria() {
        // Reset các tiêu chí tìm kiếm
        currentCategoryId = null
        currentBrandId = null
        currentSearchQuery = null

        // Reset UI cho loại sản phẩm
        val typeProductAdapter = binding.includeTypeProductHome.rcTypeProductHome.adapter as? MyAdapterRecyclerViewTypeProductHomeUser
        typeProductAdapter?.updateSelectedPosition(0)

        // Reset UI cho thương hiệu
        val brandAdapter = binding.includeBrandHome.rcBrandHome.adapter as? MyAdapterRecyclerViewBrandHomeUser
        brandAdapter?.updateSelectedPosition(0)

        // Reset SearchView
        binding.svSearchHomeAdmin.setQuery("", false)

        // Thực hiện tìm kiếm với tiêu chí đã reset
        performSearch(null, null, null)
    }
    // Hàm thực hiện tìm kiếm và hiển thị kết quả
    private fun performSearch(categoryId: String? = null, brandId: String? = null, searchQuery: String? = null) {
        try {
            // Thực hiện tìm kiếm với các tiêu chí đã chọn
            productViewModel.searchProducts(categoryId, brandId, searchQuery).observe(viewLifecycleOwner) { products ->

                try {
                    // Chuyển đổi danh sách sản phẩm thành ItemRecyclerViewProductHomeUser
                    val items = products.mapNotNull { product ->
                        try {
                            ItemRecyclerViewProductHomeUser(
                                id = product.id,
                                brandId = product.brandId,
                                categoryId = product.categoryId,
                                name = product.name,
                                price = product.price,
                                rating = product.rating,
                                description = product.description,
                                sizes = product.sizes,
                                colors = product.colors
                            )
                        } catch (e: Exception) {
                            Log.e("ProductHomeUser", "Error mapping product: ${e.message}")
                            null
                        }
                    }
                    // Lấy adapter hiện tại và cập nhật dữ liệu
                    val adapter = binding.includeProductHome.rcProductHome.adapter as? MyAdapterRecyclerViewProductHomeUser
                    adapter?.updateData(items)

                    // Cuộn RecyclerView lên đầu
                    binding.includeProductHome.rcProductHome.scrollToPosition(0)

                } catch (e: Exception) {
                    Log.e("ProductHomeUser", "Error updating search results: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ProductHomeUser", "Error performing search: ${e.message}")
        }
    }
}