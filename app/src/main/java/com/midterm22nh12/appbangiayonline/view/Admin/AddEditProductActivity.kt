package com.midterm22nh12.appbangiayonline.view.Admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.midterm22nh12.appbangiayonline.databinding.EditEndAddProductAdminBinding

class AddEditProductActivity : AppCompatActivity() {
    private lateinit var binding: EditEndAddProductAdminBinding
    private var isEditMode = false
    private var productId: String? = null // Giả định mỗi sản phẩm có ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditEndAddProductAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        val bundle = intent.extras
        if (bundle != null) {
            isEditMode = true
            binding.tvTitle.text = "Chỉnh sửa sản phẩm"
            binding.btnSubmit.text = "Cập nhật"
            productId = bundle.getString("productId")

            binding.etName.setText(bundle.getString("name"))
            binding.etPrice.setText(bundle.getString("price"))
            binding.etDescription.setText(bundle.getString("description"))
            // TODO: set spinnerBrand, spinnerCategory, spinnerStatus nếu có adapter
        } else {
            binding.tvTitle.text = "Thêm sản phẩm"
            binding.btnSubmit.text = "Thêm"
        }

        binding.btnSubmit.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val price = binding.etPrice.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val brand = binding.spinnerBrand.selectedItem.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            val status = binding.spinnerStatus.selectedItem.toString()

            if (name.isEmpty() || price.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else {
                if (isEditMode) {
                    updateProduct(productId ?: "", name, price, description, brand, category, status)
                } else {
                    addProduct(name, price, description, brand, category, status)
                }
            }
        }
    }

    private fun addProduct(
        name: String,
        price: String,
        description: String,
        brand: String,
        category: String,
        status: String
    ) {
        // TODO: Gửi dữ liệu lên Firebase hoặc DB
        Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateProduct(
        id: String,
        name: String,
        price: String,
        description: String,
        brand: String,
        category: String,
        status: String
    ) {
        // TODO: Cập nhật sản phẩm trong Firebase hoặc DB
        Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show()
        finish()
    }
}