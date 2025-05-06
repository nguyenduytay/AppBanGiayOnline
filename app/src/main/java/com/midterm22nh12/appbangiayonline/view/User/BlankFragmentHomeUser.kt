package com.midterm22nh12.appbangiayonline.view.User

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewTypeProductHomeUser

class BlankFragmentHomeUser : Fragment() {
    private lateinit var bindingFragmentHomeUser: FragmentBlankHomeUserBinding
    private lateinit var bindingNotificationProductHomeUser: NotificationProductHomeUserBinding
    private lateinit var bindingBrandHomeUser: BrandHomeUserBinding
    private lateinit var bindingProductHomeUser: ProductHomeUserBinding
    private lateinit var bindingTypeProductHomeUser: TypeProductHomeUserBinding
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

        //hiểm  thị thông bóa
        showNotification()
        //  hiển thi thông báo sản phẩm
        showNotificationProduct()
        //hiển thị nhãn hàng
        showBrand()
        //hiển thị danh sách giày
        showProduct()
        //sự kiện tìm kiếm
        searchHomeUser()
        // sự kiện lựa chọn sản phẩm theo loại
        showTypeProduct()
        //thiết lập sự kiện recyclerView ẩn hiện menu
        setupRecyclerView()
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
        //tạo danh sách list ví dụ
        val list = listOf(
            ItemRecyclerViewBrandHomeUser(R.drawable.cat1, "Adidas"),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat2, "Nike"),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat3, "Puma"),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat4, "Skechers"),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat5, "Reebok"),
            ItemRecyclerViewBrandHomeUser(R.drawable.cat6, "Lacoste")
        )
        val adapter = MyAdapterRecyclerViewBrandHomeUser(list)
        bindingBrandHomeUser.rcBrandHome.adapter = adapter
    }

    //hiển thị danh sách sản phẩm
    private fun showProduct() {
        bindingProductHomeUser.rcProductHome.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        //tạo danh sách ví dụ
        val list = listOf(
            ItemRecyclerViewProductHomeUser(
                false,
                R.drawable.shoes3,
                4.5f,
                3000000.0,
                "Giày Jordan"
            ),
            ItemRecyclerViewProductHomeUser(
                true,
                R.drawable.shoes2,
                4.6f,
                4000000.0,
                "Giày Jordan"
            ),
            ItemRecyclerViewProductHomeUser(
                false,
                R.drawable.shoes1,
                4.7f,
                3500000.0,
                "Giày Jordan"
            ),
            ItemRecyclerViewProductHomeUser(true, R.drawable.shoes, 4.8f, 2500000.0, "Giày Jordan"),
            ItemRecyclerViewProductHomeUser(
                false,
                R.drawable.shoes3,
                4.5f,
                3000000.0,
                "Giày Jordan"
            ),
            ItemRecyclerViewProductHomeUser(false, R.drawable.n1, 4.6f, 4000000.0, "Giày Jordan"),
            ItemRecyclerViewProductHomeUser(true, R.drawable.n2, 4.7f, 3500000.0, "Giày Jordan"),
            ItemRecyclerViewProductHomeUser(false, R.drawable.n3, 4.8f, 2500000.0, "Giày Jordan"),
            ItemRecyclerViewProductHomeUser(
                false,
                R.drawable.shoes3,
                4.5f,
                3000000.0,
                "Giày Jordan"
            ),
        )
        val adapter = MyAdapterRecyclerViewProductHomeUser(list)
        bindingProductHomeUser.rcProductHome.adapter = adapter
    }

    // sự kiện lựa chọn sản phẩm theo loại
    private fun showTypeProduct() {
        bindingTypeProductHomeUser.rcTypeProductHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val list = listOf(
            ItemRecyclerViewTypeProductHomeUser("Giày"),
            ItemRecyclerViewTypeProductHomeUser("Áo"),
            ItemRecyclerViewTypeProductHomeUser("Quần"),
            ItemRecyclerViewTypeProductHomeUser("Tất"),
            ItemRecyclerViewTypeProductHomeUser("Băng quấn"),
            ItemRecyclerViewTypeProductHomeUser("Lót giày"),
        )
        val adapter = MyAdapterRecyclerViewTypeProductHomeUser(list)
        bindingTypeProductHomeUser.rcTypeProductHome.adapter = adapter

    }

    //sự kiện tìm kiếm
    private fun searchHomeUser() {
        //cho full kích thước chiều ngang
        bindingFragmentHomeUser.svSearchHomeUser.maxWidth = Int.MAX_VALUE

        bindingFragmentHomeUser.svSearchHomeUser.setOnSearchClickListener {
            // Ẩn các thành phần khác khi SearchView mở
            bindingFragmentHomeUser.llHelloHomeUser.visibility = View.GONE
            bindingFragmentHomeUser.ivNotificationHomeUser.visibility = View.GONE
            bindingFragmentHomeUser.ivMessageHomeUser.visibility = View.GONE
            bindingNotificationProductHomeUser.notificationHome.visibility = View.GONE
            bindingTypeProductHomeUser.typeProductHome.visibility = View.VISIBLE
        }

        bindingFragmentHomeUser.svSearchHomeUser.setOnCloseListener {
            // Hiện lại các thành phần khác khi SearchView đóng
            bindingFragmentHomeUser.llHelloHomeUser.visibility = View.VISIBLE
            bindingFragmentHomeUser.ivNotificationHomeUser.visibility = View.VISIBLE
            bindingFragmentHomeUser.ivMessageHomeUser.visibility = View.VISIBLE
            bindingNotificationProductHomeUser.notificationHome.visibility = View.VISIBLE
            bindingTypeProductHomeUser.typeProductHome.visibility = View.GONE
            false // Trả về false để cho phép SearchView tiếp tục xử lý sự kiện đóng
        }
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
    //thiết lập ẩn hiện trạng ẩn hiện menu chính khi vuốt xem sản phẩm
    private fun setupRecyclerView()
    {
        bindingProductHomeUser.rcProductHome.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        (activity as? MainActivityUser)?.showBottomNav()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // Người dùng đang kéo - ẩn menu
                        (activity as? MainActivityUser)?.hideBottomNav()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        // Đang trượt tự động sau khi thả tay - ẩn menu
                        (activity as? MainActivityUser)?.hideBottomNav()
                    }
                }
            }
        })
    }

}