package com.midterm22nh12.appbangiayonline.view.User

import android.content.Context
import android.view.View
import com.midterm22nh12.appbangiayonline.databinding.OrderUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class order_user(
    private val context: Context,
    private val binding: OrderUserBinding,
    private val item: ItemRecyclerViewProductHomeUser
) {
    init {
        setUpView()
    }

    private fun setUpView() {
        binding.ivBackOrderUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).navigateFromOverlayToFragment(1)
        }
        binding.llReviewCountOrderUser.setOnClickListener {
            (context as? MainActivityUser)?.showRatingUser()
        }
        binding.ivCartOrderUser.setOnClickListener {
            (context as? MainActivityUser)?.navigateFromOverlayToFragment(0)
        }
        binding.ivChatOrderUser.setOnClickListener {
            (context as? MainActivityUser)?.showMessagesOverlay(item)
        }
    }
}