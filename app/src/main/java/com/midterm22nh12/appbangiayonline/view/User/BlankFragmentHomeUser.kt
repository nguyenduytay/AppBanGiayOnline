package com.midterm22nh12.appbangiayonline.view.User

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.BrandHomeUserBinding
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankHomeUserBinding
import com.midterm22nh12.appbangiayonline.databinding.NotificationProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewBrandHomeUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewNotificationProductHomeUser
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewBrandHomeUser
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewNotificationProductHomeUser
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewTypeProductHomeUser
import com.midterm22nh12.appbangiayonline.databinding.ProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.databinding.TypeProductHomeUserBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Category
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewTypeProductHomeUser
import com.midterm22nh12.appbangiayonline.viewmodel.BrandViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.CategoryViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.ProductViewModel

class BlankFragmentHomeUser : Fragment() {
    private lateinit var bindingFragmentHomeUser: FragmentBlankHomeUserBinding
    private lateinit var bindingNotificationProductHomeUser: NotificationProductHomeUserBinding
    private lateinit var bindingBrandHomeUser: BrandHomeUserBinding
    private lateinit var bindingProductHomeUser: ProductHomeUserBinding
    private lateinit var bindingTypeProductHomeUser: TypeProductHomeUserBinding

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingFragmentHomeUser = FragmentBlankHomeUserBinding.inflate(inflater, container, false)
        bindingNotificationProductHomeUser =
            NotificationProductHomeUserBinding.bind(bindingFragmentHomeUser.includeNotificationHome.notificationHome)
        bindingBrandHomeUser =
            BrandHomeUserBinding.bind(bindingFragmentHomeUser.includeBrandHome.brandHome)
        bindingProductHomeUser =
            ProductHomeUserBinding.bind(bindingFragmentHomeUser.includeProductHome.productHome)
        bindingTypeProductHomeUser =
            TypeProductHomeUserBinding.bind(bindingFragmentHomeUser.includeTypeProductHome.typeProductHome)

        // Khởi tạo ViewModel
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        brandViewModel = ViewModelProvider(this)[BrandViewModel::class.java]
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        //hiểm  thị thông bóa
        showNotification()
        //  hiển thi thông báo sản phẩm
        showNotificationProduct()
        //hiển thị nhãn hàng
        showBrand()
        // sự kiện lựa chọn sản phẩm theo loại
        showTypeProduct()
        //hiển thị danh sách giày
        showProduct()
        //sự kiện tìm kiếm
        searchHomeUser()
        //hiển thị giao diện tin nhắn
        showMessageHomeUser()
        return bindingFragmentHomeUser.root
    }

    //hiển thi thông báo
    private fun showNotificationProduct() {
        bindingNotificationProductHomeUser.rcHomeNotification.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        //tạo danh sách list ví dụ
        val list = listOf(
            ItemRecyclerViewNotificationProductHomeUser(
                "Sản phẩm mới ra mắt",
                "Giảm 50% cho mỗi sản phẩm",
                R.drawable.shoes
            ),
            ItemRecyclerViewNotificationProductHomeUser(
                "Sản phẩm mới ra mắt",
                "Giảm 50% cho mỗi sản phẩm",
                R.drawable.shoes1
            ),
            ItemRecyclerViewNotificationProductHomeUser(
                "Sản phẩm mới ra mắt",
                "Giảm 50% cho mỗi sản phẩm",
                R.drawable.shoes2
            ),
            ItemRecyclerViewNotificationProductHomeUser(
                "Sản phẩm mới ra mắt",
                "Giảm 50% cho mỗi sản phẩm",
                R.drawable.shoes3
            )
        )
        val adapter = MyAdapterRecyclerViewNotificationProductHomeUser(list)
        bindingNotificationProductHomeUser.rcHomeNotification.adapter = adapter

    }

    //hiển thị nhãn hàng
    private fun showBrand() {
        bindingBrandHomeUser.rcBrandHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = MyAdapterRecyclerViewBrandHomeUser(emptyList(),
            object : MyAdapterRecyclerViewBrandHomeUser.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewBrandHomeUser, position: Int) {
                }
            })
        bindingBrandHomeUser.rcBrandHome.adapter = adapter
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
        bindingProductHomeUser.rcProductHome.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)

        // Tạo adapter với xử lý sự kiện click
        val adapter = MyAdapterRecyclerViewProductHomeUser(emptyList(),
            object : MyAdapterRecyclerViewProductHomeUser.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewProductHomeUser, position: Int) {
                    // Thêm try-catch để bắt lỗi khi chuyển màn hình
                    try {
                        (activity as? MainActivityUser)?.showOrderUser(item)
                    } catch (e: Exception) {
                        Log.e("ProductHomeUser", "Error on item click: ${e.message}")
                        // Hiển thị thông báo lỗi nếu cần
                    }
                }

                override fun onFavoriteClick(item: ItemRecyclerViewProductHomeUser, position: Int) {
                    // Xử lý sự kiện favorite click
                }
            }
        )
        bindingProductHomeUser.rcProductHome.adapter = adapter

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
        bindingTypeProductHomeUser.rcTypeProductHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = MyAdapterRecyclerViewTypeProductHomeUser(emptyList(),
            object : MyAdapterRecyclerViewTypeProductHomeUser.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewTypeProductHomeUser, position: Int) {
                }
            })
        bindingTypeProductHomeUser.rcTypeProductHome.adapter = adapter
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

    //sự kiện tìm kiếm
    private fun searchHomeUser() {
        //cho full kích thước chiều ngang
        bindingFragmentHomeUser.svSearchHomeUser.maxWidth = Int.MAX_VALUE

        // Xử lý khi mở SearchView
        bindingFragmentHomeUser.svSearchHomeUser.setOnSearchClickListener {
            // Ẩn các thành phần khác khi SearchView mở
            bindingFragmentHomeUser.llHelloHomeUser.visibility = View.GONE
            bindingFragmentHomeUser.ivNotificationHomeUser.visibility = View.GONE
            bindingFragmentHomeUser.ivMessageHomeUser.visibility = View.GONE
            bindingNotificationProductHomeUser.notificationHome.visibility = View.GONE
            bindingTypeProductHomeUser.typeProductHome.visibility = View.VISIBLE
            (activity as MainActivityUser).hideBottomNav()
        }
        // Xử lý khi đóng SearchView
        bindingFragmentHomeUser.svSearchHomeUser.setOnCloseListener {
            // Hiện lại các thành phần khác khi SearchView đóng
            bindingFragmentHomeUser.llHelloHomeUser.visibility = View.VISIBLE
            bindingFragmentHomeUser.ivNotificationHomeUser.visibility = View.VISIBLE
            bindingFragmentHomeUser.ivMessageHomeUser.visibility = View.VISIBLE
            bindingNotificationProductHomeUser.notificationHome.visibility = View.VISIBLE
            bindingTypeProductHomeUser.typeProductHome.visibility = View.GONE
            (activity as MainActivityUser).showBottomNav()

            // Ẩn bàn phím
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(bindingFragmentHomeUser.svSearchHomeUser.windowToken, 0)
            // Reset tìm kiếm và hiển thị lại tất cả sản phẩm
            resetAllSearchCriteria()
            false // Trả về false để cho phép SearchView tiếp tục xử lý sự kiện đóng
        }
        // Xử lý sự kiện tìm kiếm
        bindingFragmentHomeUser.svSearchHomeUser.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
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
        val typeProductAdapter = bindingTypeProductHomeUser.rcTypeProductHome.adapter as? MyAdapterRecyclerViewTypeProductHomeUser
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
        val brandAdapter = bindingBrandHomeUser.rcBrandHome.adapter as? MyAdapterRecyclerViewBrandHomeUser
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
                    val adapter = bindingProductHomeUser.rcProductHome.adapter as? MyAdapterRecyclerViewProductHomeUser
                    adapter?.updateData(items)

                    // Cuộn RecyclerView lên đầu
                    bindingProductHomeUser.rcProductHome.scrollToPosition(0)

                } catch (e: Exception) {
                    Log.e("ProductHomeUser", "Error updating search results: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ProductHomeUser", "Error performing search: ${e.message}")
        }
    }
    // Phương thức reset tất cả tiêu chí tìm kiếm và UI
    private fun resetAllSearchCriteria() {
        // Reset các tiêu chí tìm kiếm
        currentCategoryId = null
        currentBrandId = null
        currentSearchQuery = null

        // Reset UI cho loại sản phẩm
        val typeProductAdapter = bindingTypeProductHomeUser.rcTypeProductHome.adapter as? MyAdapterRecyclerViewTypeProductHomeUser
        typeProductAdapter?.updateSelectedPosition(0)

        // Reset UI cho thương hiệu
        val brandAdapter = bindingBrandHomeUser.rcBrandHome.adapter as? MyAdapterRecyclerViewBrandHomeUser
        brandAdapter?.updateSelectedPosition(0)

        // Reset SearchView
        bindingFragmentHomeUser.svSearchHomeUser.setQuery("", false)

        // Thực hiện tìm kiếm với tiêu chí đã reset
        performSearch(null, null, null)
    }
    //thiết lập sự kiện hiển thị thông bao
    private fun showNotification()
    {
        // Thay vì điều khiển trực tiếp NavigationDrawer trong Fragment
        bindingFragmentHomeUser.ivNotificationHomeUser.setOnClickListener {
            // Gọi phương thức từ MainActivity để mở drawer
            (activity as? MainActivityUser)?.openNotificationDrawer()
            (activity as? MainActivityUser)?.setupItemNotification()
        }
    }
    //sự kiện hiển thị tin nhắn
    private fun showMessageHomeUser()
    {
        bindingFragmentHomeUser.ivMessageHomeUser.setOnClickListener {
            (activity as? MainActivityUser)?.also {
                if (!it.isDrawerOpen()) {
                    it.showMessagesOverlay()
                }
            }
        }
    }

}