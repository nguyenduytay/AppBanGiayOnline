package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewColorOrderView
import com.midterm22nh12.appbangiayonline.Adapter.User.MyAdapterRecyclerViewSizeOrderView
import com.midterm22nh12.appbangiayonline.Adapter.User.ProductImagePagerAdapter
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.OrderUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewColorOrderUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewSizeOrderView

class order_user(
    private val context: Context,
    private val binding: OrderUserBinding,
    private val item: ItemRecyclerViewProductHomeUser
) {
    private lateinit var colorAdapter: MyAdapterRecyclerViewColorOrderView
    init {
        setUpView()
        displayProductData()
    }

    private fun setUpView() {
        binding.ivBackOrderUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).navigateFromOverlayToFragment(1)
        }
        binding.llReviewCountOrderUser.setOnClickListener {
            (context as? MainActivityUser)?.showRatingUser()
        }
        binding.ivCartOrderUser.setOnClickListener {
            (context as? MainActivityUser)?.navigateFromOverlayToFragment(0)
        }
        binding.ivChatOrderUser.setOnClickListener {
            (context as? MainActivityUser)?.showMessagesOverlay(item)
        }
    }
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun displayProductData()
    {
        binding.tvNameProductOrderUser.text = item.name
        binding.tvPriceOrderUser.text = String.format("%,d vnđ", item.price)
        binding.tvRatingProductOrderUser.text = item.rating.toString()
        binding.tvDescriptionOrderUser.text = item.description
        binding.tvSizeOrderUser.text=item.sizes[0].value
        binding.tvStockProductOrderUser.text = "Kho: ${item.colors[0].stock}"

        // Thiết lập danh sách màu sắc TRƯỚC
        setupColorList()

        // Thiết lập ViewPager2 SAU
        setupViewPager()

        // Thiết lập kích thước
        setupSizes()

    }
    private fun setupSizes() {
        binding.rcSizeProductOrderUser.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val adapterSize = MyAdapterRecyclerViewSizeOrderView(emptyList(),
            object : MyAdapterRecyclerViewSizeOrderView.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewSizeOrderView, position: Int) {
                    binding.tvSizeOrderUser.text = item.size
                }
            })

        binding.rcSizeProductOrderUser.adapter = adapterSize

        val sizeItems = item.sizes.map { size ->
            ItemRecyclerViewSizeOrderView(size = size.value)
        }

        adapterSize.updateData(sizeItems)

        // Đặt kích thước mặc định
        if (sizeItems.isNotEmpty()) {
            binding.tvSizeOrderUser.text = sizeItems[0].size
        }
    }
    private fun setupViewPager() {
        try {
            // Kiểm tra dữ liệu
            if (item.colors.isEmpty()) {
                android.util.Log.e("ViewPager", "Lỗi: Danh sách màu trống")
                return
            }

            val images = item.colors.map { it.image }

            // Adapter cho ViewPager2
            val pagerAdapter = ProductImagePagerAdapter(images)
            binding.viewPagerProductImagesOrderUser.adapter = pagerAdapter
            binding.viewPagerProductImagesOrderUser.orientation = ViewPager2.ORIENTATION_HORIZONTAL

            // Đăng ký callback để theo dõi thay đổi trang
            binding.viewPagerProductImagesOrderUser.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    @SuppressLint("SetTextI18n")
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        // Các cập nhật khác về màu, stock, v.v...
                        if (::colorAdapter.isInitialized) {
                            colorAdapter.updateSelectedPosition(position)
                        }

                        if (position >= 0 && position < colorAdapter.itemCount) {
                            binding.rcListColorOrderUser.smoothScrollToPosition(position)
                        }

                        if (position >= 0 && position < item.colors.size) {
                            val color = item.colors[position]
                            binding.tvColorOrderUser.text = "Màu: ${color.name}"
                            binding.tvStockProductOrderUser.text = "Kho: ${color.stock}"
                        }
                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                        // Cập nhật hiệu ứng dot indicators mượt mà hơn
                        updateDotIndicatorsSmooth(position, positionOffset)
                    }
                }
            )

            // Thiết lập nút điều hướng
            binding.btNextLeftOrderUser.setOnClickListener {
                val currentItem = binding.viewPagerProductImagesOrderUser.currentItem
                if (currentItem > 0) {
                    binding.viewPagerProductImagesOrderUser.setCurrentItem(currentItem - 1, true)
                }
            }

            binding.btNextRightOrderUser.setOnClickListener {
                val currentItem = binding.viewPagerProductImagesOrderUser.currentItem
                if (currentItem < item.colors.size - 1) {
                    binding.viewPagerProductImagesOrderUser.setCurrentItem(currentItem + 1, true)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewPager", "Lỗi trong setupViewPager: ${e.message}")
            e.printStackTrace()
        }
    }

    // Cập nhật dot indicators với hiệu ứng mượt mà dựa trên vị trí và offset
    private fun updateDotIndicatorsSmooth(position: Int, positionOffset: Float) {
        try {
            val totalPages = item.colors.size

            if (totalPages <= 1) {
                return
            }

            if (totalPages == 2) {
                if (position == 0) {
                    // Đang lướt từ trang 1 sang trang 2
                    if (positionOffset > 0) {
                        // Nếu có offset (đang di chuyển), tạo hiệu ứng chuyển đổi
                        if (positionOffset < 0.5) {
                            // Chưa đi qua 50% - dot1 vẫn đậm, dot2 vẫn nhạt
                            binding.dot1.background = ContextCompat.getDrawable(context, R.drawable.indicator_selected)
                            binding.dot2.background = ContextCompat.getDrawable(context, R.drawable.indicator_unselected)
                        } else {
                            // Đã đi qua 50% - đổi trạng thái
                            binding.dot1.background = ContextCompat.getDrawable(context, R.drawable.indicator_unselected)
                            binding.dot2.background = ContextCompat.getDrawable(context, R.drawable.indicator_selected)
                        }
                    } else {
                        // Không có offset (đang đứng yên ở trang 1)
                        binding.dot1.background = ContextCompat.getDrawable(context, R.drawable.indicator_selected)
                        binding.dot2.background = ContextCompat.getDrawable(context, R.drawable.indicator_unselected)
                    }
                } else {
                    // Đang lướt từ trang 2 về trang 1
                    if (positionOffset > 0) {
                        // Đây là trường hợp đặc biệt không thường xảy ra
                    } else {
                        // Đang đứng yên ở trang 2
                        binding.dot1.background = ContextCompat.getDrawable(context, R.drawable.indicator_unselected)
                        binding.dot2.background = ContextCompat.getDrawable(context, R.drawable.indicator_selected)
                    }
                }
                return
            }

            // Với nhiều hơn 2 trang, sử dụng nguyên tắc chung
            val currentProgress = (position + positionOffset) / (totalPages - 1)

            if (currentProgress <= 0.5) {
                // Nửa đầu của tổng số trang: dot1 đậm, dot2 nhạt
                binding.dot1.background = ContextCompat.getDrawable(context, R.drawable.indicator_selected)
                binding.dot2.background = ContextCompat.getDrawable(context, R.drawable.indicator_unselected)
            } else {
                // Nửa sau của tổng số trang: dot1 nhạt, dot2 đậm
                binding.dot1.background = ContextCompat.getDrawable(context, R.drawable.indicator_unselected)
                binding.dot2.background = ContextCompat.getDrawable(context, R.drawable.indicator_selected)
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewPager", "Lỗi khi cập nhật dot indicators mượt mà: ${e.message}")
            e.printStackTrace()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun setupColorList() {
        binding.rcListColorOrderUser.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Chuyển đổi dữ liệu từ model sản phẩm sang model thumbnail
        val colorItems = item.colors.map { color ->
            ItemRecyclerViewColorOrderUser(
                name = color.name,
                image = color.image,
                productCode = color.productCode,
                stock = color.stock
            )
        }

        // Khởi tạo adapter
        colorAdapter = MyAdapterRecyclerViewColorOrderView(colorItems,
            object : MyAdapterRecyclerViewColorOrderView.OnItemClickListener {
                override fun onItemClick(item: ItemRecyclerViewColorOrderUser, position: Int) {
                    // Khi nhấn vào thumbnail, cập nhật ViewPager2
                    binding.viewPagerProductImagesOrderUser.setCurrentItem(position, true)
                    binding.tvColorOrderUser.text = "Màu: ${item.name}"
                    binding.tvStockProductOrderUser.text = "Kho: ${item.stock}"
                }
            }
        )

        binding.rcListColorOrderUser.adapter = colorAdapter

        // Đặt màu mặc định (đầu tiên)
        if (colorItems.isNotEmpty()) {
            binding.tvColorOrderUser.text = "Màu: ${colorItems[0].name}"
            binding.tvStockProductOrderUser.text = "Kho: ${colorItems[0].stock}"
        }
    }
    //sự kiện mua hàng

}