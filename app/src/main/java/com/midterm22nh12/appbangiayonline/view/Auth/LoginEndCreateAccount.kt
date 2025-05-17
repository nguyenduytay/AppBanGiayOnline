package com.midterm22nh12.appbangiayonline.view.Auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ActivityLoginEndCreateAccountBinding
import com.midterm22nh12.appbangiayonline.view.Admin.MainActivityAdmin
import com.midterm22nh12.appbangiayonline.view.User.MainActivityUser
import com.midterm22nh12.appbangiayonline.viewmodel.AuthViewModel

/**
 * Activity quản lý màn hình đăng nhập và đăng ký tài khoản
 * Xử lý các thao tác xác thực người dùng và chuyển hướng đến màn hình phù hợp
 */
class LoginEndCreateAccount : AppCompatActivity() {
    private lateinit var binding: ActivityLoginEndCreateAccountBinding
    private lateinit var authViewModel: AuthViewModel

    /**
     * Khởi tạo Activity, thiết lập UI và các thành phần cần thiết
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEndCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //khởi taạ view model
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        //kiểm tra nếu người dùng đã đăng nhập
        if (authViewModel.isUserLoggedIn()) {
            //chuyển đến màn hình chính
            navigateToMainScreen()
            return
        }
        // Hiển thị màn hình login đầu tiên
        binding.viewFlipperMain.displayedChild = 0

        setupLoginPasswordVisibility()         //sự kiện hiển thị mật khẩu trong đăng nhập
        setupCreateAccountPasswordVisibility() //sự kiện hiển thị mật khẩu trong đăng kí
        setupNavigationButtons()               // chuyển trang đăng nhập và đăng kí qua link
        setupLoginButton()
        setupObservers()
    }

    /**
     * Thiết lập chức năng hiển thị/ẩn mật khẩu cho màn hình đăng nhập
     * Sử dụng TextWatcher để cập nhật icon hiển thị mật khẩu khi người dùng nhập liệu
     */
    private fun setupLoginPasswordVisibility() {
        var isPasswordVisible = false
        binding.loginLayout.edPasswordInputLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordVisibilityIcon(
                    s.toString(),
                    binding.loginLayout.ivPasswordLogin,
                    isPasswordVisible
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.loginLayout.ivPasswordLogin.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(
                binding.loginLayout.edPasswordInputLogin,
                binding.loginLayout.ivPasswordLogin,
                isPasswordVisible
            )
        }
    }

    /**
     * Thiết lập chức năng hiển thị/ẩn mật khẩu và xác nhận mật khẩu cho màn hình đăng ký
     * Kiểm tra tính hợp lệ của mật khẩu và hiển thị đánh giá độ mạnh mật khẩu
     */
    private fun setupCreateAccountPasswordVisibility() {
        var isPasswordVisible = false
        var isRePasswordVisible = false

        // Ẩn TextView thông báo lỗi ban đầu
        binding.createAccountLayout.tvRePasswordCreateAccount.visibility = View.GONE

        // Xử lý hiển thị icon cho mật khẩu
        binding.createAccountLayout.edPasswordInputCreate.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordVisibilityIcon(
                    s.toString(),
                    binding.createAccountLayout.ivPasswordCreate,
                    isPasswordVisible
                )

                // Kiểm tra mật khẩu có khớp không
                val password = s.toString()
                val rePassword = binding.createAccountLayout.edRePasswordInputCreate.text.toString()

                if (rePassword.isNotEmpty()) {
                    val passwordsMatch = password == rePassword
                    binding.createAccountLayout.tvRePasswordCreateAccount.text =
                        "Mật khẩu không khớp"
                    binding.createAccountLayout.tvRePasswordCreateAccount.visibility =
                        if (passwordsMatch) View.GONE else View.VISIBLE
                }
                //đánh giá độ mạnh của mật khẩu
                if (password.isNotEmpty()) {
                    binding.createAccountLayout.llPowerPasswordCreateAccount.visibility =
                        View.VISIBLE
                    val strength = evaluatePassword(password)
                    powerPassword(strength)
                } else {
                    binding.createAccountLayout.llPowerPasswordCreateAccount.visibility = View.GONE
                }

            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Xử lý hiển thị icon cho nhập lại mật khẩu
        binding.createAccountLayout.edRePasswordInputCreate.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordVisibilityIcon(
                    s.toString(),
                    binding.createAccountLayout.ivRePasswordCreate,
                    isRePasswordVisible
                )

                // Kiểm tra mật khẩu có khớp không
                val password = binding.createAccountLayout.edPasswordInputCreate.text.toString()
                val rePassword = s.toString()

                if (rePassword.isEmpty()) {
                    // Nếu trường nhập lại mật khẩu trống, ẩn thông báo lỗi
                    binding.createAccountLayout.tvRePasswordCreateAccount.visibility = View.GONE
                } else if (password.isEmpty()) {
                    // Nếu trường mật khẩu trống nhưng trường nhập lại không trống
                    binding.createAccountLayout.tvRePasswordCreateAccount.text =
                        "Vui lòng nhập mật khẩu trước"
                    binding.createAccountLayout.tvRePasswordCreateAccount.visibility = View.VISIBLE
                } else {
                    // Cả hai trường đều có giá trị, kiểm tra xem chúng có khớp không
                    val passwordsMatch = password == rePassword
                    binding.createAccountLayout.tvRePasswordCreateAccount.text =
                        "Mật khẩu không khớp"
                    binding.createAccountLayout.tvRePasswordCreateAccount.visibility =
                        if (passwordsMatch) View.GONE else View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Sự kiện click vào icon hiển thị mật khẩu
        binding.createAccountLayout.ivPasswordCreate.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(
                binding.createAccountLayout.edPasswordInputCreate,
                binding.createAccountLayout.ivPasswordCreate,
                isPasswordVisible
            )
        }

        // Sự kiện click vào icon hiển thị nhập lại mật khẩu
        binding.createAccountLayout.ivRePasswordCreate.setOnClickListener {
            isRePasswordVisible = !isRePasswordVisible
            togglePasswordVisibility(
                binding.createAccountLayout.edRePasswordInputCreate,
                binding.createAccountLayout.ivRePasswordCreate,
                isRePasswordVisible
            )
        }
    }

    /**
     * Cập nhật biểu tượng hiển thị mật khẩu dựa trên trạng thái và nội dung đầu vào
     *
     * @param input Chuỗi văn bản đầu vào
     * @param imageView ImageView hiển thị biểu tượng
     * @param isVisible Trạng thái hiển thị mật khẩu hiện tại
     */
    private fun updatePasswordVisibilityIcon(
        input: String,
        imageView: ImageView,
        isVisible: Boolean
    ) {
        if (input.isEmpty()) {
            imageView.setImageResource(R.drawable.account_login)
        } else {
            imageView.setImageResource(
                if (isVisible) R.drawable.visibility_on else R.drawable.visibility_off
            )
        }
    }

    /**
     * Chuyển đổi hiển thị/ẩn mật khẩu trong EditText
     *
     * @param editText EditText chứa mật khẩu
     * @param imageView ImageView hiển thị biểu tượng
     * @param isVisible Trạng thái hiển thị mật khẩu mới
     */
    private fun togglePasswordVisibility(
        editText: EditText,
        imageView: ImageView,
        isVisible: Boolean
    ) {
        editText.inputType = if (isVisible) {
            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        imageView.setImageResource(if (isVisible) R.drawable.visibility_on else R.drawable.visibility_off)
        editText.setSelection(editText.text.length)
    }

    /**
     * Thiết lập các nút chuyển đổi giữa màn hình đăng nhập và đăng ký
     */
    private fun setupNavigationButtons() {
        // Chuyển đến trang đăng ký
        binding.loginLayout.tvLinkCreateAccountLogin.setOnClickListener {
            binding.viewFlipperMain.displayedChild = 1 // Chuyển sang màn hình đăng ký
        }

        // Quay lại trang đăng nhập
        binding.createAccountLayout.tvLinkLoginCreate.setOnClickListener {
            binding.viewFlipperMain.displayedChild = 0 // Chuyển về màn hình đăng nhập
        }
    }

    /**
     * Chuyển hướng người dùng đến màn hình chính dựa vào quyền (admin hoặc user)
     */
    private fun navigateToMainScreen() {
        authViewModel.isAdminUser { isAdmin ->
            if (isAdmin) {
                startActivity(Intent(this, MainActivityAdmin::class.java))
            } else {
                startActivity(Intent(this, MainActivityUser::class.java))
            }
            finish()
        }
    }

    /**
     * Thiết lập các observer để lắng nghe sự thay đổi trạng thái từ ViewModel
     * Xử lý kết quả đăng nhập, đăng ký và hiển thị loading
     */
    private fun setupObservers() {
        //loading
        authViewModel.isLoading.observe(this) { isLoading ->
            binding.loginLayout.progressBarLogin.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.createAccountLayout.progressBarCreateAccount.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }
        //đăng nhập
        authViewModel.loginResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = { _ ->
                        //đăng nhập thành công chuyển đến màn hình chính
                        navigateToMainScreen()
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        //hiển thị thông báo lỗi
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    })
            }
        }
        //đăng kí
        authViewModel.registrationResult.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                result.fold(
                    onSuccess = { _ ->
                        //đăng kí thành công chuyển về màn hình đăng nhập
                        Toast.makeText(this, "Đăng kí thành công", Toast.LENGTH_SHORT).show()
                        binding.viewFlipperMain.displayedChild = 0
                    },
                    onFailure = { exception ->
                        //hiển thị thông báo lỗi
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }

    /**
     * Thiết lập các nút đăng nhập và đăng ký
     * Thu thập thông tin đầu vào và gọi phương thức tương ứng từ ViewModel
     */
    private fun setupLoginButton() {
        binding.loginLayout.btLogin.setOnClickListener {
            val userInput = binding.loginLayout.edUsernameInputLogin.text.toString().trim()
            val password = binding.loginLayout.edPasswordInputLogin.text.toString()

            authViewModel.loginUser(userInput, password)
        }

        binding.createAccountLayout.btCreateAccount.setOnClickListener {
            val fullName =
                binding.createAccountLayout.edFirstNameInputCreate.text.toString() + binding.createAccountLayout.edLastnameInputCreate.text.toString()
            val username = binding.createAccountLayout.edUserNameInputCreate.text.toString().trim()
            val email = binding.createAccountLayout.edEmailInputCreate.text.toString().trim()
            val phone = binding.createAccountLayout.edPhoneInputCreate.text.toString().trim()
            val password = binding.createAccountLayout.edPasswordInputCreate.text.toString()
            val rePassword = binding.createAccountLayout.edRePasswordInputCreate.text.toString()

            authViewModel.registerUser(fullName, username, email, phone, password, rePassword)
        }
    }

    /**
     * Hiển thị đánh giá độ mạnh của mật khẩu bằng các thanh màu
     *
     * @param index Mức độ mạnh của mật khẩu (1-5)
     */
    private fun powerPassword(index: Int) {
        val powerPassword = arrayOf(
            binding.createAccountLayout.tvPower1,
            binding.createAccountLayout.tvPower2,
            binding.createAccountLayout.tvPower3,
            binding.createAccountLayout.tvPower4,
            binding.createAccountLayout.tvPower5
        )

        //hiển thị mức độ mật khẩu
        for (i in 0 until index) {
            powerPassword[i].setTextColor(Color.parseColor("#FFFFFF"))
        }
        //màu sắc dựa trên mức độ
        val colors = when (index) {
            1 -> Color.parseColor("#D02727")
            2 -> Color.parseColor("#F80303")
            3 -> Color.parseColor("#427C48")
            4 -> Color.parseColor("#68AF39")
            5 -> Color.parseColor("#11E629")
            else -> Color.parseColor("#FFFFFF")
        }
        // Cập nhật thông báo
        val message = when (index) {
            1 -> "Rất yếu"
            2 -> "Yếu"
            3 -> "Trung bình"
            4 -> "Mạnh"
            5 -> "Rất mạnh"
            else -> ""
        }
        //hiển thị các thanh với màu sắc tương ứng
        for (i in 0 until index) {
            powerPassword[i].visibility = View.VISIBLE
            powerPassword[i].setBackgroundColor(colors)
        }
        //hiển thị thông báo về độ mạnh yếu cảu mật khẩu
        binding.createAccountLayout.tvMessagePowerPasswordCreate.text = message
        binding.createAccountLayout.tvMessagePowerPasswordCreate.setTextColor(colors)
    }

    /**
     * Đánh giá độ mạnh của mật khẩu dựa trên các tiêu chí:
     * - Độ dài
     * - Đa dạng ký tự (in hoa, in thường, số)
     * - Ký tự đặc biệt
     *
     * @param password Mật khẩu cần đánh giá
     * @return Mức độ mạnh từ 1-5 (1: rất yếu, 5: rất mạnh)
     */
    private fun evaluatePassword(password: String): Int {
        if (password.isEmpty()) return 1

        var score = 0

        // Độ dài
        when {
            password.length >= 8 -> score += 2
            password.length >= 5 -> score += 1
        }

        // Đa dạng ký tự
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1

        // Ký tự đặc biệt
        val specialChars = "!@#$%^&*()-_=+[]{}|;:,.<>?/~"
        if (password.any { specialChars.contains(it) }) score += 1

        // Trả về mức độ từ 1-5
        return when {
            score <= 1 -> 1  // Rất yếu
            score == 2 -> 2  // Yếu
            score == 3 -> 3  // Trung bình
            score == 4 -> 4  // Mạnh
            score > 5 -> 5  // Rất mạnh
            else -> 1
        }
    }
}