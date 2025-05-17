package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.midterm22nh12.appbangiayonline.Utils.LocationUtil
import com.midterm22nh12.appbangiayonline.databinding.FragmentBlankAccountUserBinding
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel

/**
 * Fragment quản lý tài khoản người dùng
 */
class BlankFragmentAccountUser : Fragment() {
    // region Biến
    private lateinit var binding: FragmentBlankAccountUserBinding
    private lateinit var authViewModel: AuthViewModel
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBlankAccountUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo ViewModel
        authViewModel = (requireActivity() as MainActivityUser).getSharedViewModel()

        // Hiển thị màn hình tài khoản đầu tiên
        binding.viewFlipperAccountUser.displayedChild = 0

        // Thiết lập các chức năng
        setupNavigation()
        setupAddress()
        setupUserInfo()
        setupLogout()
    }

    override fun onPause() {
        super.onPause()
        // Dừng cập nhật vị trí khi fragment không hiển thị
        LocationUtil.stopLocationUpdates()
    }
    // Thêm hàm onResume để luôn cập nhật khi hiển thị lại
    override fun onResume() {
        super.onResume()
        if (authViewModel.isUserLoggedIn()) {
            // Load lại dữ liệu mỗi khi fragment hiển thị
            authViewModel.loadCurrentUserInfo()
        }
    }
    // region Permission Handling
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAddressFromLocation()
            } else {
                showLocationPermissionError()
            }
        }
    }
    // endregion

    // region Setup Methods
    /**
     * Thiết lập thông tin người dùng
     */
    private fun setupUserInfo() {
        // Observer cho tên người dùng
        authViewModel.userName.observe(viewLifecycleOwner) { userName ->
            binding.includeAccountUser.tvUsernameAccountUser.text = userName
        }

        // Observer cho thông tin người dùng
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.includeAccountStepUser.apply {
                tvFullNameAccountSetupUser.text = user?.fullName
                tvEmailAccountSetupUser.text = user?.email
                tvPhoneAccountSetupUser.text = user?.phone
            }
        }

        // Tải thông tin nếu đã đăng nhập
        if (authViewModel.isUserLoggedIn()) {
            authViewModel.loadUserName()
            authViewModel.loadCurrentUserInfo()
        }
    }

    /**
     * Thiết lập tính năng đăng xuất
     */
    private fun setupLogout() {
        binding.includeAccountStepUser.btLogoutAccountSetupUser.setOnClickListener {
            (activity as? MainActivityUser)?.logout()
        }
    }

    /**
     * Thiết lập tính năng điều hướng
     */
    private fun setupNavigation() {
        // Điều hướng giữa các màn hình trong ViewFlipper
        setupViewFlipperNavigation()

        // Điều hướng đến các màn hình khác
        setupExternalNavigation()
    }

    /**
     * Thiết lập điều hướng giữa các màn hình trong ViewFlipper
     */
    private fun setupViewFlipperNavigation() {
        // Chuyển từ màn hình thông tin sang màn hình cài đặt
        binding.includeAccountUser.ivSettingAccountUser.setOnClickListener {
            binding.viewFlipperAccountUser.displayedChild = 1
        }

        // Quay lại từ màn hình cài đặt về màn hình thông tin
        binding.includeAccountStepUser.ivBackAccountSetupUser.setOnClickListener {
            binding.viewFlipperAccountUser.displayedChild = 0
        }
    }

    /**
     * Thiết lập điều hướng đến các màn hình khác
     */
    private fun setupExternalNavigation() {
        with(binding.includeAccountUser) {
            // Quay lại màn hình chính
            ivBackAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.navigateFromOverlayToFragment(1)
            }

            // Đi đến giỏ hàng
            ivCartAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.navigateFromOverlayToFragment(0)
            }

            // Đi đến tin nhắn
            ivChatAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.also {
                    if (!it.isDrawerOpen()) {
                        it.showMessagesOverlay()
                    }
                }
            }

            // Đi đến lịch sử mua hàng
            ivPurchaseHistoryAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.also {
                    if (!it.isDrawerOpen()) {
                        it.showPurchaseHistoryUser()
                    }
                }
            }

            // Đi đến xác nhận đơn hàng
            llConfirmationUserAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.showConfirmationUser()
            }

            // Đi đến vận chuyển
            llTransportationUserAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.showTransportationUser()
            }

            // Đi đến đánh giá sản phẩm
            llMyReviewUserAccountUser.setOnClickListener {
                (activity as? MainActivityUser)?.showRatingUser()
            }
        }

        // Đi đến bảo mật tài khoản
        binding.includeAccountStepUser.llAccountPasswordAccountSetupUser.setOnClickListener {
            (activity as? MainActivityUser)?.showAccountSecurityUser()
        }

    }
    // endregion

    // region Address Handling
    /**
     * Thiết lập tính năng quản lý địa chỉ
     */
    private fun setupAddress() {
        // Thiết lập observers cho địa chỉ
        setupAddressObservers()

        // Thiết lập sự kiện cho lấy địa chỉ từ vị trí
        setupAddressLocationButton()

        // Thiết lập sự kiện cho nhập địa chỉ thủ công
        setupManualAddressInput()

        // Tải địa chỉ người dùng nếu đã đăng nhập
        if (authViewModel.isUserLoggedIn()) {
            authViewModel.loadUserAddress()
        }
    }

    /**
     * Thiết lập observers cho địa chỉ
     */
    private fun setupAddressObservers() {
        // Observer cho địa chỉ người dùng hiện tại
        authViewModel.userAddress.observe(viewLifecycleOwner) { address ->
            if (!address.isNullOrEmpty() && isValidAddress(address)) {
                binding.includeAccountStepUser.etAddressAccountSetupUser.setText(address)
            }
        }

        // Observer cho kết quả cập nhật địa chỉ
        authViewModel.addressUpdateResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        "Cập nhật địa chỉ thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                result.onFailure { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Thiết lập nút lấy địa chỉ từ vị trí
     */
    private fun setupAddressLocationButton() {
        binding.includeAccountStepUser.ivAddressAccountSetupUser.setOnClickListener {
            Toast.makeText(requireContext(), "Đang lấy địa chỉ...", Toast.LENGTH_SHORT).show()
            getAddressFromLocation()
        }
    }

    /**
     * Lấy địa chỉ từ vị trí hiện tại
     */
    private fun getAddressFromLocation() {
        LocationUtil.getAddressToTextView(
            this,
            binding.includeAccountStepUser.etAddressAccountSetupUser,
            false,
            onAddressReceived = { address -> handleReceivedAddress(address) }
        )
    }

    /**
     * Xử lý địa chỉ nhận được từ LocationUtil
     */
    private fun handleReceivedAddress(address: String) {
        if (isValidAddress(address)) {
            // Log cho mục đích debug
            Log.d(TAG, "Nhận được địa chỉ: $address")

            // Lưu trực tiếp không cần xác nhận
            authViewModel.updateUserAddress(address)
            Toast.makeText(requireContext(), "Đã tự động lưu địa chỉ", Toast.LENGTH_SHORT).show()
        } else {
            // Log lỗi
            Log.e(TAG, "Không lưu địa chỉ không hợp lệ: $address")

            if (address.isNotEmpty() && address.startsWith("Lỗi")) {
                Toast.makeText(requireContext(), address, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Thiết lập input địa chỉ thủ công
     */
    private fun setupManualAddressInput() {
        // Nút lưu địa chỉ
        binding.includeAccountStepUser.btAddressAccountSetupUser.setOnClickListener {
            val manualAddress = binding.includeAccountStepUser.etAddressAccountSetupUser.text.toString().trim()
            if (manualAddress.isNotEmpty()) {
                saveManualAddress(manualAddress)
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Lưu địa chỉ nhập thủ công
     */
    private fun saveManualAddress(address: String) {
        if (address.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.updateUserAddress(address)
        Toast.makeText(requireContext(), "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show()
        hideKeyboard()
    }
    // endregion

    // region Utility Methods
    /**
     * Kiểm tra địa chỉ có hợp lệ không
     */
    private fun isValidAddress(address: String): Boolean {
        return address.isNotEmpty() &&
                address != "Không có quyền truy cập vị trí" &&
                address != "Vui lòng cấp quyền vị trí" &&
                address != "Đang lấy vị trí hiện tại..." &&
                address != "Vui lòng bật dịch vụ định vị (GPS)" &&
                !address.startsWith("Lỗi")
    }

    /**
     * Hiển thị lỗi khi không có quyền truy cập vị trí
     */
    private fun showLocationPermissionError() {
        binding.includeAccountStepUser.etAddressAccountSetupUser.setText("Không có quyền truy cập vị trí")
        Toast.makeText(
            requireContext(),
            "Yêu cầu cấp quyền vị trí để lấy địa chỉ",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Ẩn bàn phím ảo
     */
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            binding.includeAccountStepUser.etAddressAccountSetupUser.windowToken,
            0
        )
    }
    // endregion

    companion object {
        private const val TAG = "AccountFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}