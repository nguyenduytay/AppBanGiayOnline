package com.midterm22nh12.appbangiayonline.view.Admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.midterm22nh12.appbangiayonline.Adapter.Admin.ColorProductAdminAdapter
import com.midterm22nh12.appbangiayonline.Adapter.Admin.SizeProductAdminAdapter
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.DialogAddColorBinding
import com.midterm22nh12.appbangiayonline.databinding.DialogAddSizeBinding
import com.midterm22nh12.appbangiayonline.databinding.EditEndAddProductAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Category
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductSize
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import com.midterm22nh12.appbangiayonline.viewmodel.BrandViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.CategoryViewModel
import com.midterm22nh12.appbangiayonline.viewmodel.ProductViewModel

class edit_end_add_product(
    private val context: Context,
    private val binding: EditEndAddProductAdminBinding,
    private val product: ItemRecyclerViewProductHomeUser?,
    private val lifecycleOwner: LifecycleOwner
) {
    private lateinit var productViewModel: ProductViewModel
    private lateinit var brandViewModel: BrandViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    // Lưu trữ ID của thương hiệu và danh mục được chọn
    private var selectedBrandId: String? = null
    private var selectedCategoryId: String? = null

    // Danh sách lưu trữ đối tượng Brand và Category để mapping giữa vị trí và ID
    private val brandsList = mutableListOf<Brand>()
    private val categoriesList = mutableListOf<Category>()


    // Danh sách lưu trữ kích thước và màu sắc - khởi tạo ngay khi khai báo
    private var sizesList = mutableListOf<ProductSize>()
    private val colorsList = mutableListOf<ProductColor>()

    // Adapter cho kích thước và màu sắc
    private lateinit var sizeAdapter: SizeProductAdminAdapter
    private lateinit var colorAdapter: ColorProductAdminAdapter

    init {
        setUpUI()
    }

    private fun setUpUI() {
        binding.ivBack.setOnClickListener {
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityAdmin).returnToPreviousOverlay()
        }

        productViewModel = (context as MainActivityAdmin).provideProductViewModel()
        brandViewModel = (context as MainActivityAdmin).provideBrandViewModel()
        categoryViewModel = (context as MainActivityAdmin).provideCategoryViewModel()

        // Thiết lập dữ liệu cho spinner
        setupBrandSpinner()
        setupCategorySpinner()

        // Thiết lập nút lưu và xóa
        setupSubmitButton()
        setupDeleteButton()

        // Điền thông tin sản phẩm nếu đang chỉnh sửa
        if (product != null) {
            setUpProduct()
        } else {
            setUpProductNull()
        }
    }

    private fun setupBrandSpinner() {
        // Khởi tạo adapter cho spinner thương hiệu
        val brandAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBrand.adapter = brandAdapter

        binding.spinnerBrand.viewTreeObserver   .addOnGlobalLayoutListener {
            val view = binding.spinnerBrand.selectedView as? TextView
            view?.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // Lắng nghe dữ liệu thương hiệu từ ViewModel
        brandViewModel.getBrands().observe(lifecycleOwner) { brands ->
            brandsList.clear()
            brandsList.addAll(brands)

            // Cập nhật adapter với tên các thương hiệu
            brandAdapter.clear()
            brands.forEach { brand ->
                brandAdapter.add(brand.name)
            }

            // Nếu đang chỉnh sửa, chọn thương hiệu tương ứng
            if (product != null) {
                val brandPosition = brandsList.indexOfFirst { it.id == product.brandId }
                if (brandPosition >= 0) {
                    binding.spinnerBrand.setSelection(brandPosition)
                    selectedBrandId = product.brandId
                }
            }
        }

        // Xử lý sự kiện khi người dùng chọn thương hiệu
        binding.spinnerBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < brandsList.size) {
                    selectedBrandId = brandsList[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBrandId = null
            }
        }
    }

    private fun setupCategorySpinner() {
        // Khởi tạo adapter cho spinner danh mục
        val categoryAdapter = ArrayAdapter<String>(context, android.R.layout.preference_category)
        categoryAdapter.setDropDownViewResource(android.R.layout.preference_category)
        binding.spinnerCategory.adapter = categoryAdapter

        binding.spinnerCategory.viewTreeObserver.addOnGlobalLayoutListener {
            val view = binding.spinnerCategory.selectedView as? TextView
            view?.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // Lắng nghe dữ liệu danh mục từ ViewModel
        categoryViewModel.getCategories().observe(lifecycleOwner) { categories ->
            categoriesList.clear()
            categoriesList.addAll(categories)

            // Cập nhật adapter với tên các danh mục
            categoryAdapter.clear()
            categories.forEach { category ->
                categoryAdapter.add(category.name)
            }

            // Nếu đang chỉnh sửa, chọn danh mục tương ứng
            if (product != null) {
                val categoryPosition = categoriesList.indexOfFirst { it.id == product.categoryId }
                if (categoryPosition >= 0) {
                    binding.spinnerCategory.setSelection(categoryPosition)
                    selectedCategoryId = product.categoryId
                }
            }
        }

        // Xử lý sự kiện khi người dùng chọn danh mục
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < categoriesList.size) {
                    selectedCategoryId = categoriesList[position].id
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = null
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpProduct() {
        // Điền thông tin cơ bản của sản phẩm
        binding.etIdProduct.setText(product?.id)
        binding.etName.setText(product?.name)
        binding.etPrice.setText(product?.price.toString())
        binding.etDescription.setText(product?.description)

        // Cập nhật tiêu đề để hiển thị chế độ chỉnh sửa
        binding.tvTitle.text = "Sửa sản phẩm"
        binding.btnSubmit.text = "Cập nhật"

        // Thiết lập hiển thị kích thước (cần có triển khai chip group riêng)
        setupSizesChips()

        // Thiết lập hiển thị màu sắc (cần triển khai adapter RecyclerView)
        setupColorsRecyclerView()
    }
    private fun setUpProductNull() {
        // Điền thông tin cơ bản của sản phẩm
        binding.etIdProduct.setText("")
        binding.etName.setText("")
        binding.etPrice.setText("")
        binding.etDescription.setText("")

        // Cập nhật tiêu đề để hiển thị chế độ chỉnh sửa
        binding.tvTitle.text = "Thêm sản phẩm"
        binding.btnSubmit.text = "Thêm"

        // Xóa danh sách kích thước và màu sắc
        sizesList.clear()
        colorsList.clear()

        // Thiết lập adapter và RecyclerView cho kích thước và màu sắc
        setupSizesChips()
        setupColorsRecyclerView()
    }

    private fun setupSizesChips() {

        // Khởi tạo adapter nếu chưa được khởi tạo
        if (!::sizeAdapter.isInitialized) {
            sizeAdapter = SizeProductAdminAdapter(
                context,
                sizesList,
                onSizeDeleted = { position ->
                    // Xử lý khi xóa size
                    // Không cần làm gì thêm vì adapter đã cập nhật danh sách
                },
                onSizeEdited = { size, position ->
                    showEditSizeDialog(size, position)
                }
            )
        }

        // Thiết lập RecyclerView cho sizes
        binding.rvSizes.layoutManager = LinearLayoutManager(context)
        binding.rvSizes.adapter = sizeAdapter

        // Thiết lập sự kiện cho nút thêm size
        binding.btnAddSize.setOnClickListener {
            showEditSizeDialog()
        }

        // Nếu đang chỉnh sửa sản phẩm, cập nhật danh sách sizes
        if (product != null && product.sizes.isNotEmpty()) {
            sizesList.clear()
            sizesList.addAll(product.sizes)
            sizeAdapter.updateSizes(product.sizes)
        }
    }

    private fun setupColorsRecyclerView() {

        // Khởi tạo adapter nếu chưa được khởi tạo
        if (!::colorAdapter.isInitialized) {
            colorAdapter = ColorProductAdminAdapter(
                context,
                colorsList,
                onColorDeleted = { position ->
                    // Xử lý khi xóa color
                    // Không cần làm gì thêm vì adapter đã cập nhật danh sách
                },
                onColorEdited = { color, position ->
                    showEditColorDialog(color, position)
                }
            )
        }

        // Thiết lập RecyclerView cho colors
        binding.rvColors.layoutManager = LinearLayoutManager(context)
        binding.rvColors.adapter = colorAdapter

        // Thiết lập sự kiện cho nút thêm color
        binding.btnAddColor.setOnClickListener {
            showEditColorDialog()
        }

        // Nếu đang chỉnh sửa sản phẩm, cập nhật danh sách colors
        if (product != null && product.colors.isNotEmpty()) {
            colorsList.clear()
            colorsList.addAll(product.colors)
            colorAdapter.updateColors(product.colors)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun showEditColorDialog(color: ProductColor?=null, position: Int?=null) {
        // Sử dụng binding cho dialog
        val dialogBinding = DialogAddColorBinding.inflate(LayoutInflater.from(context))
        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        // Cập nhật tiêu đề và nút
        if(color!=null && position!=null) {
            dialogBinding.tvTitle.text = "Chỉnh sửa màu sắc"
            dialogBinding.btnAddColor.text = "Cập nhật"

            // Set giá trị hiện tại
            dialogBinding.etColorName.setText(color.name)
            dialogBinding.etProductCode.setText(color.productCode)
            dialogBinding.etStock.setText(color.stock.toString())
            dialogBinding.etColorImage.setText(color.image)
        }
        else
        {
            dialogBinding.tvTitle.text = "Thêm màu sắc"
            dialogBinding.btnAddColor.text = "Thêm"
        }

        // Thiết lập adapter cho spinner
        val statusAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.size_status_array,
            android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerStatus.adapter = statusAdapter

        // Chọn trạng thái hiện tại
        val statusPosition = when (color?.status) {
            "available" -> 0
            "hidden" -> 1
            "out_of_stock" -> 2
            "coming_soon" -> 3
            else -> 0
        }
        // Thay đổi màu của text hiển thị
        dialogBinding.spinnerStatus.viewTreeObserver.addOnGlobalLayoutListener {
            val view = dialogBinding.spinnerStatus.selectedView as? TextView
            view?.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        dialogBinding.spinnerStatus.setSelection(statusPosition)

        // Xử lý sự kiện click
        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.btnAddColor.setOnClickListener {
            val colorName = dialogBinding.etColorName.text.toString().trim()
            val productCode = dialogBinding.etProductCode.text.toString().trim()
            val stockText = dialogBinding.etStock.text.toString().trim()
            val imageUrl = dialogBinding.etColorImage.text.toString().trim()

            if (colorName.isNotEmpty() && productCode.isNotEmpty() && stockText.isNotEmpty() && imageUrl.isNotEmpty()) {
                // Lấy giá trị trạng thái từ spinner
                val statusCode = when (dialogBinding.spinnerStatus.selectedItemPosition) {
                    0 -> "available"
                    1 -> "hidden"
                    2 -> "out_of_stock"
                    3 -> "coming_soon"
                    else -> "available"
                }
                val stock = stockText.toIntOrNull() ?: 0

                // Tạo đối tượng color mới/cập nhật
                val newColor = ProductColor(colorName, imageUrl, productCode, stock, statusCode)

                // Thêm hoặc cập nhật trong adapter
                if (position != null) {
                    // Cập nhật
                    colorAdapter.updateColor(newColor, position)
                } else {
                    // Thêm mới
                    colorAdapter.addColor(newColor)
                }

                // Đóng dialog
                alertDialog.dismiss()
            }else {
                // Hiển thị thông báo lỗi nếu thiếu thông tin
                AlertDialog.Builder(context)
                    .setTitle("Lỗi")
                    .setMessage("Vui lòng điền đầy đủ thông tin màu sắc!")
                    .setPositiveButton("Đồng ý", null)
                    .show()
            }
        }
        alertDialog.show()
    }

    private fun showEditSizeDialog(size: ProductSize?=null, position: Int?=null) {
        // Sử dụng binding cho dialog
        val dialogBinding = DialogAddSizeBinding.inflate(LayoutInflater.from(context))
        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()

        if(size!=null && position!=null) {
            // Cập nhật tiêu đề và nút
            dialogBinding.tvTitleAddSize.text = "Chỉnh sửa kích thước"
            dialogBinding.btnAddSize.text = "Cập nhật"

            // Set giá trị hiện tại
            dialogBinding.etSize.setText(size.value)
        }
        else
        {
            // Cập nhật tiêu đề và nút
            dialogBinding.tvTitleAddSize.text = "Thêm kích thước"
            dialogBinding.btnAddSize.text = "Thêm"

        }

        // Thiết lập adapter cho spinner
        val statusAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.size_status_array,
            android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerBrand.adapter = statusAdapter

        // Chọn trạng thái hiện tại
        val statusPosition = when (size?.status) {
            "available" -> 0
            "hidden" -> 1
            "out_of_stock" -> 2
            "coming_soon" -> 3
            else -> 0
        }
        // Thay đổi màu của text hiển thị
        dialogBinding.spinnerBrand.viewTreeObserver.addOnGlobalLayoutListener {
            val view = dialogBinding.spinnerBrand.selectedView as? TextView
            view?.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        dialogBinding.spinnerBrand.setSelection(statusPosition)

        // Xử lý sự kiện click
        dialogBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.btnAddSize.setOnClickListener {
            val sizeValue = dialogBinding.etSize.text.toString().trim()

            if (sizeValue.isNotEmpty()) {
                // Lấy giá trị trạng thái từ spinner
                val statusCode = when (dialogBinding.spinnerBrand.selectedItemPosition) {
                    0 -> "available"
                    1 -> "hidden"
                    2 -> "out_of_stock"
                    3 -> "coming_soon"
                    else -> "available"
                }

                // Tạo đối tượng size cập nhật
                val updatedSize = ProductSize(sizeValue, statusCode)

                // Cập nhật trong adapter
                if (position != null) {
                    sizeAdapter.updateSize(updatedSize, position)
                }
                else {
                    // Thêm mới
                    sizeAdapter.addSize(updatedSize)
                }

                // Đóng dialog
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateInput()) {
                if (product != null) {
                    updateProduct()
                } else {
                    addProduct()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        // Kiểm tra ID sản phẩm
        val productId = binding.etIdProduct.text.toString().trim()
        if (productId.isEmpty()) {
            showErrorMessage("Vui lòng nhập ID sản phẩm!")
            return false
        }

        // Kiểm tra tên sản phẩm
        val productName = binding.etName.text.toString().trim()
        if (productName.isEmpty()) {
            showErrorMessage("Vui lòng nhập tên sản phẩm!")
            return false
        }

        // Kiểm tra giá
        val priceText = binding.etPrice.text.toString().trim()
        if (priceText.isEmpty()) {
            showErrorMessage("Vui lòng nhập giá sản phẩm!")
            return false
        }

        // Kiểm tra thương hiệu và danh mục
        if (selectedBrandId == null) {
            showErrorMessage("Vui lòng chọn thương hiệu sản phẩm!")
            return false
        }

        if (selectedCategoryId == null) {
            showErrorMessage("Vui lòng chọn loại sản phẩm!")
            return false
        }

        // Kiểm tra danh sách kích thước
        if (sizeAdapter.getSizes().isEmpty()) {
            showErrorMessage("Vui lòng thêm ít nhất một kích thước!")
            return false
        }

        // Kiểm tra danh sách màu sắc
        if (colorAdapter.getColors().isEmpty()) {
            showErrorMessage("Vui lòng thêm ít nhất một màu sắc!")
            return false
        }

        return true
    }

    private fun showErrorMessage(message: String) {
        AlertDialog.Builder(context)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("Đồng ý", null)
            .show()
    }

    private fun addProduct() {
        // Lấy thông tin từ form
        val productId = binding.etIdProduct.text.toString().trim()
        val productName = binding.etName.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val price = priceText.toLongOrNull() ?: 0
        val description = binding.etDescription.text.toString().trim()

        // Tạo đối tượng Product mới
        val newProduct = Product(
            id = productId,
            brandId = selectedBrandId!!,
            categoryId = selectedCategoryId!!,
            name = productName,
            price = price,
            description = description,
            rating = 0.0, // Sản phẩm mới chưa có đánh giá
            sizes = sizeAdapter.getSizes(),
            colors = colorAdapter.getColors()
        )

        // Hiển thị dialog loading
        val loadingDialog = AlertDialog.Builder(context)
            .setMessage("Đang thêm sản phẩm...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Gọi phương thức thêm sản phẩm trong ViewModel
        productViewModel.addProduct(newProduct) { success, errorMessage ->
            // Đóng dialog loading
            loadingDialog.dismiss()

            if (success) {
                // Thông báo thành công và trở về màn hình trước
                AlertDialog.Builder(context)
                    .setTitle("Thành công")
                    .setMessage("Thêm sản phẩm thành công!")
                    .setPositiveButton("Đồng ý") { _, _ ->
                        binding.root.visibility = View.GONE
                        (context as MainActivityAdmin).returnToPreviousOverlay()
                    }
                    .show()
            } else {
                // Hiển thị thông báo lỗi
                AlertDialog.Builder(context)
                    .setTitle("Lỗi")
                    .setMessage("Thêm sản phẩm thất bại: $errorMessage")
                    .setPositiveButton("Đồng ý", null)
                    .show()
            }
        }
    }

    private fun updateProduct() {
        // Lấy thông tin từ form
        val productId = binding.etIdProduct.text.toString().trim()
        val productName = binding.etName.text.toString().trim()
        val priceText = binding.etPrice.text.toString().trim()
        val price = priceText.toLongOrNull() ?: 0
        val description = binding.etDescription.text.toString().trim()

        // Tạo đối tượng Product cập nhật
        val updatedProduct = Product(
            id = productId,
            brandId = selectedBrandId!!,
            categoryId = selectedCategoryId!!,
            name = productName,
            price = price,
            description = description,
            rating = product?.rating ?: 0.0, // Giữ nguyên rating cũ
            sizes = sizeAdapter.getSizes(),
            colors = colorAdapter.getColors()
        )

        // Hiển thị dialog loading
        val loadingDialog = AlertDialog.Builder(context)
            .setMessage("Đang cập nhật sản phẩm...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Gọi phương thức cập nhật sản phẩm trong ViewModel
        productViewModel.updateProduct(updatedProduct) { success, errorMessage ->
            // Đóng dialog loading
            loadingDialog.dismiss()

            if (success) {
                // Thông báo thành công và trở về màn hình trước
                AlertDialog.Builder(context)
                    .setTitle("Thành công")
                    .setMessage("Cập nhật sản phẩm thành công!")
                    .setPositiveButton("Đồng ý") { _, _ ->
                        binding.root.visibility = View.GONE
                        (context as MainActivityAdmin).returnToPreviousOverlay()
                    }
                    .show()
            } else {
                // Hiển thị thông báo lỗi
                AlertDialog.Builder(context)
                    .setTitle("Lỗi")
                    .setMessage("Cập nhật sản phẩm thất bại: $errorMessage")
                    .setPositiveButton("Đồng ý", null)
                    .show()
            }
        }
    }

    private fun setupDeleteButton() {
        // Nếu đang trong chế độ chỉnh sửa, hiển thị nút xóa
        if (product != null) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                // Hiển thị dialog xác nhận xóa
                AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                    .setPositiveButton("Xóa") { _, _ ->
                        deleteProduct()
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        } else {
            // Nếu đang trong chế độ thêm mới, ẩn nút xóa
            binding.btnDelete.visibility = View.GONE
        }
    }

    private fun deleteProduct() {
        // Hiển thị dialog loading
        val loadingDialog = AlertDialog.Builder(context)
            .setMessage("Đang xóa sản phẩm...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Gọi phương thức xóa sản phẩm trong ViewModel
        productViewModel.deleteProduct(product!!.id) { success, errorMessage ->
            // Đóng dialog loading
            loadingDialog.dismiss()

            if (success) {
                // Thông báo thành công và trở về màn hình trước
                AlertDialog.Builder(context)
                    .setTitle("Thành công")
                    .setMessage("Xóa sản phẩm thành công!")
                    .setPositiveButton("Đồng ý") { _, _ ->
                        binding.root.visibility = View.GONE
                        (context as MainActivityAdmin).returnToPreviousOverlay()
                    }
                    .show()
            } else {
                // Hiển thị thông báo lỗi
                AlertDialog.Builder(context)
                    .setTitle("Lỗi")
                    .setMessage("Xóa sản phẩm thất bại: $errorMessage")
                    .setPositiveButton("Đồng ý", null)
                    .show()
            }
        }
    }
}