package com.midterm22nh12.appbangiayonline.view.User

import android.content.Context
import android.view.View
import com.midterm22nh12.appbangiayonline.databinding.RatingUserBinding


class rating_user(private val context: Context, private val binding: RatingUserBinding) {
    init {
        setUpView()
    }

    private fun setUpView() {
        binding.ivBackRatingUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
    }
}