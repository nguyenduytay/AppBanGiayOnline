package com.midterm22nh12.appbangiayonline.view.User

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ShopMessagesUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser

class messages_user(
    private val context: Context,
    private val binding: ShopMessagesUserBinding,
    private val item: ItemRecyclerViewProductHomeUser? = null
) {

    init {
        setUpView()
        showProductMessage()
    }

    private fun setUpView() {
        binding.ivBackShopMessagesUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
        binding.btByNowShopMessagesUser.setOnClickListener {
            // Ẩn giao diện tin nhắn
            binding.root.visibility = View.GONE
            // Quay lại trang trước
            (context as MainActivityUser).returnToPreviousOverlay()
        }
    }
    //hiển thị sản phẩm
    @SuppressLint("DefaultLocale")
    private fun showProductMessage()
    {
        if(item==null)
        {
            binding.llSuggestShopMessagesUser.visibility=View.GONE
        }else
        {
            binding.llSuggestShopMessagesUser.visibility=View.VISIBLE
            binding.tvNameProductShopMessageUser.text=item.name
            binding.tvPriceProductShopMessagesUser.text= String.format("%,d vnđ", item.price)
            Glide.with(binding.ivImageProductShopMessagesUser.context)
                .load(item.colors[0].image)
                .placeholder(R.drawable.shoes1)
                .error(R.drawable.shoes1)
                .into(binding.ivImageProductShopMessagesUser)
        }
    }
}