package com.midterm22nh12.appbangiayonline.view.Admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.Adapter.Admin.MyAdapterRecyclerViewBillAdmin
import com.midterm22nh12.appbangiayonline.Utils.UiState
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankBillAdminBinding
import com.midterm22nh12.appbangiayonline.viewmodel.OrderViewModel

class BlankFragmentBillAdmin : Fragment() {

    private var _binding: FragmentBlankBillAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var orderViewModel: OrderViewModel
    private lateinit var adapter: MyAdapterRecyclerViewBillAdmin

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlankBillAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo ViewModel
        orderViewModel = ViewModelProvider(requireActivity()).get(OrderViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        setupObservers()

        // Tải danh sách đơn hàng
        (activity as MainActivityAdmin).loadOrders()
    }
    override fun onResume() {
        super.onResume()
        (activity as MainActivityAdmin).loadOrders() // Tải lại đơn hàng mỗi khi Fragment hiển thị lại
    }
    private fun setupToolbar() {
        binding.billAdmin.ivBack.setOnClickListener {
            (activity as MainActivityAdmin).returnToPreviousOverlay()
        }

        binding.billAdmin.titleText.text = "Quản lý đơn hàng"
    }

    private fun setupRecyclerView() {
        adapter = MyAdapterRecyclerViewBillAdmin(
            onItemDetailClick = { listProduct ->
                // Xử lý khi nhấn vào xem chi tiết đơn hàng
                (activity as MainActivityAdmin).showOrderDetail(listProduct)
            },
            onProcessOrderClick = { orderId ->
                // Xử lý khi nhấn vào nút xử lý đơn hàng
            }
        )

        binding.billAdmin.rcBillAdmin.apply {
            adapter = this@BlankFragmentBillAdmin.adapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Quan sát kết quả từ ViewModel
        orderViewModel.allOrdersWithItemsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.billAdmin.progressBar.visibility = View.VISIBLE
                    binding.billAdmin.rcBillAdmin.visibility = View.GONE
                }
                is UiState.Success -> {
                    binding.billAdmin.progressBar.visibility = View.GONE

                    if (state.data.isEmpty()) {
                        // Hiển thị thông báo không có đơn hàng
                        Toast.makeText(requireContext(), "Không có đơn hàng nào", Toast.LENGTH_SHORT).show()
                        binding.billAdmin.rcBillAdmin.visibility = View.GONE
                    } else {
                        binding.billAdmin.rcBillAdmin.visibility = View.VISIBLE
                        // Cập nhật adapter với dữ liệu đã lấy được
                        adapter.updateData(state.data)
                    }
                }
                is UiState.Error -> {
                    binding.billAdmin.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}