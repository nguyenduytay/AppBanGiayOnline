package com.midterm22nh12.appbangiayonline.view.Admin

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.midterm22nh12.appbangiayonline.Adapter.Admin.RecentOrdersAdapter
import com.midterm22nh12.appbangiayonline.Adapter.Admin.TopProductsAdapter
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankRevenueAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Order.OrderWithItems
import com.midterm22nh12.appbangiayonline.model.Item.TopProduct
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BlankFragmentRevenueAdmin : Fragment() {

    private var _binding: FragmentBlankRevenueAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderViewModel: OrderViewModel
    private lateinit var topProductsAdapter: TopProductsAdapter
    private lateinit var recentOrdersAdapter: RecentOrdersAdapter

    private var selectedTimeRange = 0 // 0: Hôm nay, 1: 7 ngày, 2: 30 ngày, 3: Quý này, 4: Năm nay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlankRevenueAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        setupListeners()
        loadData()
    }

    private fun setupViewModel() {
        orderViewModel = (activity as MainActivityAdmin).provideOrderViewModel()
    }

    private fun setupUI() {
        // Thiết lập nút đăng xuất
        binding.btnLogout.setOnClickListener {
            (activity as MainActivityAdmin).logout()
        }

        // Thiết lập Spinner
        setupSpinner()

        // Thiết lập RecyclerViews
        setupRecyclerViews()

        // Thiết lập biểu đồ
        setupChart()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.time_ranges,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTimeRange.adapter = adapter

        // Thay đổi màu của text hiển thị
        binding.spinnerTimeRange.viewTreeObserver.addOnGlobalLayoutListener {
            val view = binding.spinnerTimeRange.selectedView as? TextView
            view?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }
    private fun setupRecyclerViews() {
        // Thiết lập RecyclerView cho sản phẩm bán chạy
        topProductsAdapter = TopProductsAdapter(requireContext(), emptyList())
        binding.rvTopProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopProducts.adapter = topProductsAdapter

        // Thiết lập RecyclerView cho đơn hàng gần đây
        recentOrdersAdapter = RecentOrdersAdapter(requireContext(), emptyList())
        binding.rvRecentOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentOrders.adapter = recentOrdersAdapter
    }

    private fun setupChart() {
        val barChart = binding.chartRevenue

        // Thiết lập các thuộc tính cơ bản của biểu đồ
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true
        barChart.setDrawGridBackground(false)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)
        barChart.setPinchZoom(false)
        barChart.setScaleEnabled(false)

        // Thiết lập trục X
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Thiết lập trục Y bên trái
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f

        // Thiết lập trục Y bên phải
        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false
    }

    private fun setupListeners() {
        // Lắng nghe sự kiện khi người dùng chọn khoảng thời gian khác
        binding.spinnerTimeRange.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTimeRange = position
                Log.e("OrderViewModel_tay", "Success ${selectedTimeRange}")
                // Tải dữ liệu theo khoảng thời gian mới
                loadDataByTimeRange(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Không làm gì
            }
        }

        // Lắng nghe sự kiện khi có dữ liệu đơn hàng mới
        orderViewModel.allOrdersWithItemsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    processOrdersData(state.data)
                }
                is UiState.Loading -> {
                    // Hiển thị loading nếu cần
                }
                is UiState.Error -> {
                }
            }
        }
    }

    private fun loadData() {
        // Tải tất cả đơn hàng
        orderViewModel.getAllOrdersWithItemsRevenue()
    }

    private fun loadDataByTimeRange(position: Int) {
        // Tải dữ liệu theo khoảng thời gian đã chọn
        // Đã có dữ liệu từ getAllOrdersWithItems(), chỉ cần lọc lại
        val state = orderViewModel.allOrdersWithItemsStateRevenue.value
        if (state is UiState.Success) {
            processOrdersData(state.data)
            Log.e("OrderViewModel_tay", "Success ${state.data}")
        }
    }

    private fun processOrdersData(allOrders: List<OrderWithItems>) {
        // Lọc đơn hàng theo khoảng thời gian
        val filteredOrders = filterOrdersByTimeRange(allOrders, selectedTimeRange)

        // Tính toán doanh thu theo khoảng thời gian
        calculateRevenue(filteredOrders)

        // Tính toán sản phẩm bán chạy
        calculateTopProducts(filteredOrders)

        // Hiển thị đơn hàng gần đây
        displayRecentOrders(filteredOrders)

        // Cập nhật biểu đồ
        updateRevenueChart(filteredOrders)
    }

    private fun filterOrdersByTimeRange(orders: List<OrderWithItems>, timeRange: Int): List<OrderWithItems> {
        val calendar = Calendar.getInstance()
        val currentTime = calendar.timeInMillis

        return when (timeRange) {
            0 -> { // Hôm nay
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                orders.filter { it.order.createdAt >= startOfDay }
            }
            1 -> { // 7 ngày qua
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                orders.filter { it.order.createdAt >= weekAgo }
            }
            2 -> { // 30 ngày qua
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val monthAgo = calendar.timeInMillis
                orders.filter { it.order.createdAt >= monthAgo }
            }
            3 -> { // Quý này
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentQuarter = currentMonth / 3
                calendar.set(Calendar.MONTH, currentQuarter * 3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfQuarter = calendar.timeInMillis
                orders.filter { it.order.createdAt >= startOfQuarter }
            }
            4 -> { // Năm nay
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfYear = calendar.timeInMillis
                orders.filter { it.order.createdAt >= startOfYear }
            }
            else -> orders
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateRevenue(orders: List<OrderWithItems>) {
        // Tính tổng doanh thu
        val totalRevenue = orders.sumOf { it.order.totalAmount }
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        // Hiển thị doanh thu hôm nay
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        val todayOrders = orders.filter { it.order.createdAt >= startOfDay }
        val todayRevenue = todayOrders.sumOf { it.order.totalAmount }

        binding.tvTodayRevenue.text = "${numberFormat.format(todayRevenue)} đ"

        // Hiển thị số đơn hàng mới (đơn hàng hôm nay)
        binding.tvNewOrders.text = todayOrders.size.toString()

        // Hiển thị tổng số đơn hàng
        binding.tvTotalOrders.text = orders.size.toString()
    }

    private fun calculateTopProducts(orders: List<OrderWithItems>) {
        // Tạo map để đếm số lượng bán của từng sản phẩm
        val productSales = mutableMapOf<String, TopProduct>()

        // Duyệt qua từng đơn hàng và từng sản phẩm trong đơn hàng
        for (order in orders) {
            for (item in order.items) {
                val productId = item.productId
                val currentProduct = productSales[productId]
                Log.e("OrderViewModel_tay", "productId: $productId, currentProduct: $currentProduct")
                if (currentProduct == null) {
                    // Nếu sản phẩm chưa có trong map, thêm mới
                    productSales[productId] = TopProduct(
                        productId = productId,
                        productName = item.productName,
                        productImage = item.productImage,
                        quantity = item.quantity,
                        revenue = item.price * item.quantity
                    )
                } else {
                    // Nếu sản phẩm đã có trong map, cập nhật số lượng và doanh thu
                    productSales[productId] = currentProduct.copy(
                        quantity = currentProduct.quantity + item.quantity,
                        revenue = currentProduct.revenue + (item.price * item.quantity)
                    )
                }
            }
        }

        // Sắp xếp sản phẩm theo doanh thu giảm dần
        val topProducts = productSales.values.sortedByDescending { it.revenue }.take(5)

        // Cập nhật adapter
        topProductsAdapter.updateData(topProducts)
    }

    private fun displayRecentOrders(orders: List<OrderWithItems>) {
        // Sắp xếp đơn hàng theo thời gian tạo giảm dần
        val recentOrders = orders.sortedByDescending { it.order.createdAt }.take(5)

        // Cập nhật adapter
        recentOrdersAdapter.updateData(recentOrders)
    }

    private fun updateRevenueChart(orders: List<OrderWithItems>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        when (selectedTimeRange) {
            0 -> { // Hôm nay (theo giờ)
                val hourlyRevenue = calculateHourlyRevenue(orders)
                for (i in hourlyRevenue.indices) {
                    entries.add(BarEntry(i.toFloat(), hourlyRevenue[i].toFloat()))
                    labels.add("${i}h")
                }
            }
            1 -> { // 7 ngày qua
                val dailyRevenue = calculateDailyRevenue(orders, 7)
                for (i in dailyRevenue.indices) {
                    entries.add(BarEntry(i.toFloat(), dailyRevenue[i].toFloat()))

                    // Lấy tên ngày
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, -(6 - i))
                    val dayFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    labels.add(dayFormat.format(calendar.time))
                }
            }
            2 -> { // 30 ngày qua (theo tuần)
                val weeklyRevenue = calculateWeeklyRevenue(orders, 4)
                for (i in weeklyRevenue.indices) {
                    entries.add(BarEntry(i.toFloat(), weeklyRevenue[i].toFloat()))
                    labels.add("Tuần ${i + 1}")
                }
            }
            3, 4 -> { // Quý này hoặc năm nay (theo tháng)
                val monthlyRevenue = calculateMonthlyRevenue(orders, if (selectedTimeRange == 3) 3 else 12)
                for (i in monthlyRevenue.indices) {
                    entries.add(BarEntry(i.toFloat(), monthlyRevenue[i].toFloat()))

                    // Lấy tên tháng
                    val calendar = Calendar.getInstance()
                    if (selectedTimeRange == 3) {
                        // Quý này
                        val currentMonth = calendar.get(Calendar.MONTH)
                        val currentQuarter = currentMonth / 3
                        calendar.set(Calendar.MONTH, currentQuarter * 3 + i)
                    } else {
                        // Năm nay
                        calendar.set(Calendar.MONTH, i)
                    }
                    val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
                    labels.add(monthFormat.format(calendar.time))
                }
            }
        }

        // Tạo dataset
        val dataSet = BarDataSet(entries, "Doanh thu")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.status_processing)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        // Tạo BarData
        val data = BarData(dataSet)

        // Thiết lập định dạng trục X
        binding.chartRevenue.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Cập nhật biểu đồ
        binding.chartRevenue.data = data
        binding.chartRevenue.invalidate()
    }

    private fun calculateHourlyRevenue(orders: List<OrderWithItems>): List<Long> {
        val hourlyRevenue = MutableList(24) { 0L }

        // Lấy thời gian bắt đầu của ngày hôm nay
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // Lọc đơn hàng của ngày hôm nay
        val todayOrders = orders.filter { it.order.createdAt >= startOfDay }

        // Tính doanh thu theo giờ
        for (order in todayOrders) {
            val orderCalendar = Calendar.getInstance()
            orderCalendar.timeInMillis = order.order.createdAt
            val hour = orderCalendar.get(Calendar.HOUR_OF_DAY)
            hourlyRevenue[hour] += order.order.totalAmount
        }

        return hourlyRevenue
    }

    private fun calculateDailyRevenue(orders: List<OrderWithItems>, days: Int): List<Long> {
        val dailyRevenue = MutableList(days) { 0L }

        // Lấy thời gian bắt đầu của ngày days ngày trước
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -(days - 1))
        val startTime = calendar.timeInMillis

        // Lọc đơn hàng từ ngày bắt đầu
        val filteredOrders = orders.filter { it.order.createdAt >= startTime }

        // Tính doanh thu theo ngày
        for (order in filteredOrders) {
            val orderCalendar = Calendar.getInstance()
            orderCalendar.timeInMillis = order.order.createdAt

            val dayDiff = ((orderCalendar.timeInMillis - startTime) / (24 * 60 * 60 * 1000)).toInt()
            if (dayDiff in 0..<days) {
                dailyRevenue[dayDiff] += order.order.totalAmount
            }
        }

        return dailyRevenue
    }

    private fun calculateWeeklyRevenue(orders: List<OrderWithItems>, weeks: Int): List<Long> {
        val weeklyRevenue = MutableList(weeks) { 0L }

        // Lấy thời gian bắt đầu của tuần 4 tuần trước
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.WEEK_OF_YEAR, -(weeks - 1))
        val startTime = calendar.timeInMillis

        // Lọc đơn hàng từ ngày bắt đầu
        val filteredOrders = orders.filter { it.order.createdAt >= startTime }

        // Tính doanh thu theo tuần
        for (order in filteredOrders) {
            val orderCalendar = Calendar.getInstance()
            orderCalendar.timeInMillis = order.order.createdAt

            val weekDiff = ((orderCalendar.timeInMillis - startTime) / (7 * 24 * 60 * 60 * 1000)).toInt()
            if (weekDiff >= 0 && weekDiff < weeks) {
                weeklyRevenue[weekDiff] += order.order.totalAmount
            }
        }

        return weeklyRevenue
    }

    private fun calculateMonthlyRevenue(orders: List<OrderWithItems>, months: Int): List<Long> {
        val monthlyRevenue = MutableList(months) { 0L }

        // Lấy thời gian bắt đầu của tháng months tháng trước
        val calendar = Calendar.getInstance()
        if (months == 3) {
            // Quý này
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentQuarter = currentMonth / 3
            calendar.set(Calendar.MONTH, currentQuarter * 3)
        } else {
            // Năm nay
            calendar.set(Calendar.MONTH, 0)
        }
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // Lọc đơn hàng từ ngày bắt đầu
        val filteredOrders = orders.filter { it.order.createdAt >= startTime }

        // Tính doanh thu theo tháng
        for (order in filteredOrders) {
            val orderCalendar = Calendar.getInstance()
            orderCalendar.timeInMillis = order.order.createdAt

            var monthIndex: Int
            if (months == 3) {
                // Quý này
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentQuarter = currentMonth / 3
                val quarterStartMonth = currentQuarter * 3
                monthIndex = orderCalendar.get(Calendar.MONTH) - quarterStartMonth
            } else {
                // Năm nay
                monthIndex = orderCalendar.get(Calendar.MONTH)
            }

            if (monthIndex in 0..<months) {
                monthlyRevenue[monthIndex] += order.order.totalAmount
            }
        }

        return monthlyRevenue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}