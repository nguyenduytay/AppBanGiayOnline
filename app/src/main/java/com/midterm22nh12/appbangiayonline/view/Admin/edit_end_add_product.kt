package com.midterm22nh12.appbangiayonline.view.Admin

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.midterm22nh12.appbangiayonline.databinding.EditEndAddProductAdminBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class edit_end_add_product(
    private val context: Context,
    private val binding: EditEndAddProductAdminBinding,
    private val product: ItemRecyclerViewProductHomeUser?,
    private val lifecycleOwner: LifecycleOwner
) {
    init {
        setUpUI()
    }
    private fun setUpUI()
    {
        binding.ivBack.setOnClickListener {
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityAdmin).returnToPreviousOverlay()
        }
    }
}