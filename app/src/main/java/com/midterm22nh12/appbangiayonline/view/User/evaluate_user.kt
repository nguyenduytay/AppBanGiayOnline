package com.midterm22nh12.appbangiayonline.view.User

import android.content.Context
import android.view.View
import com.midterm22nh12.appbangiayonline.databinding.EvaluateUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation


class evaluate_user(private val context: Context, private val binding: EvaluateUserBinding,private val item : ItemRecyclerViewConfirmation) {
    init {
        setUpView()
    }

    private fun setUpView() {
        binding.ivBackEvaluateUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
    }
}