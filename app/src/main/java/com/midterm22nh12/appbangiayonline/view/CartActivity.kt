package com.midterm22nh12.appbangiayonline.view

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.viewmodel.CartAdapter
import com.midterm22nh12.appbangiayonline.model.CartItem

class CartActivity : AppCompatActivity(), CartAdapter.OnQuantityChangeListener, CartAdapter.OnItemCheckListener {

    private lateinit var listViewCart: ListView
    private lateinit var adapter: CartAdapter
    private val cartItems = mutableListOf<CartItem>()

    // UI Elements
    private lateinit var checkAll: CheckBox
    private lateinit var tvTotal: TextView
    private lateinit var btnBuy: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: TextView
    private lateinit var btnSMS: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.shopping_cart)

        initViews()
        loadCartItems()
        setupListeners()
        updateTotalSummary()
    }

    private fun initViews() {
        // Initialize ListView and adapter
        listViewCart = findViewById(R.id.listViewCart)

        // Initialize other UI elements
        checkAll = findViewById(R.id.check_All)
        tvTotal = findViewById(R.id.tv_Total)
        btnBuy = findViewById(R.id.btnBuy)
        btnBack = findViewById(R.id.btn_Back)
        btnDelete = findViewById(R.id.btn_Delete)
        btnSMS = findViewById(R.id.btn_sms)
    }

    private fun loadCartItems() {
        // Add sample items (in a real app, you would load these from a database or API)
        cartItems.add(
            CartItem(
                id = 1,
                name = "Giày Jordan - hàng m",
                color = "4",
                size = "32",
                imageResource = R.drawable.shoes2,
                quantity = 0
            )
        )

        cartItems.add(
            CartItem(
                id = 2,
                name = "Giày Jordan - hàng yyt",
                color = "5",
                size = "32",
                imageResource = R.drawable.shoes3,
                quantity = 0
            )
        )

        cartItems.add(
            CartItem(
                id = 3,
                name = "Giày Jordan - hàng th",
                color = "2",
                size = "32",
                imageResource = R.drawable.shoes,
                quantity = 0
            )
        )

        // Set up adapter
        adapter = CartAdapter(this, cartItems, this, this)
        listViewCart.adapter = adapter
    }

    private fun setupListeners() {
        // Set up checkbox listener
        checkAll.setOnCheckedChangeListener { _, isChecked ->
            adapter.updateAllSelection(isChecked)
            updateTotalSummary()
        }

        // Set up navigation buttons
        btnBack.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            if (adapter.getSelectedItemCount() > 0) {
                adapter.removeSelectedItems()
                updateTotalSummary()
                checkAll.isChecked = false
                Toast.makeText(this, "Đã xóa các mục đã chọn", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng chọn sản phẩm để xóa", Toast.LENGTH_SHORT).show()
            }
        }

        btnSMS.setOnClickListener {
            Toast.makeText(this, "Hiển thị thêm tùy chọn", Toast.LENGTH_SHORT).show()
        }

        // Set up buy button
        btnBuy.setOnClickListener {
            val selectedCount = adapter.getSelectedItemCount()
            if (selectedCount > 0) {
                Toast.makeText(this, "Tiến hành thanh toán", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTotalSummary() {
        val selectedCount = adapter.getSelectedItemCount()

        // In a real app, you would calculate the actual total price here
        // For now, we just show "0 đ" as per the screenshot
        tvTotal.text = "0 đ"

        // Update the buy button text
        btnBuy.text = "Mua ($selectedCount)"
    }

    override fun onQuantityChanged(position: Int, newQuantity: Int) {
        updateTotalSummary()
    }

    override fun onItemChecked(position: Int, isChecked: Boolean) {
        // Check if all items are checked
        val allChecked = cartItems.all { it.isSelected }

        // Update the "check all" checkbox without triggering its listener
        if (checkAll.isChecked != allChecked) {
            checkAll.isChecked = allChecked
        }

        updateTotalSummary()
    }
}