package com.midterm22nh12.appbangiayonline.Adapter.User


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemOrderReviewProductUserBinding
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewOrderReviewProductUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdapterRecyclerViewOrderReviewProductUser(
    private var reviews: List<ItemRecyclerViewOrderReviewProductUser> = emptyList(),
) : RecyclerView.Adapter<MyAdapterRecyclerViewOrderReviewProductUser.ReviewViewHolder>() {


    inner class ReviewViewHolder(val binding: ItemOrderReviewProductUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemOrderReviewProductUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        Log.d("ReviewProductAdapter", "onCreateViewHolder được gọi")
        return ReviewViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        try {
            val review = reviews[position]
            with(holder.binding) {
                // Hiển thị thông tin người dùng
                tvUserName.text = review.userName

                if (review.images.isNotEmpty()) {
                    // Chỉ lấy ảnh đầu tiên làm avatar
                    Glide.with(ivUserAvatar.context)
                        .load(review.images[0])
                        .placeholder(R.drawable.emoji)
                        .error(R.drawable.account_circle)
                        .circleCrop()
                        .into(ivUserAvatar)
                } else {
                    // Sử dụng avatar mặc định
                    ivUserAvatar.setImageResource(R.drawable.account_circle)
                }

                // Hiển thị đánh giá sao
                ratingBar.rating = review.rating

                // Hiển thị chi tiết sản phẩm mua
                tvReviewContent.text = "Sản phẩm : ${review.productName} " +" Màu: ${review.colorName} " + "Size: ${review.size}"

                // Hiển thị nội dung đánh giá
                tvReviewContent.text = review.comment

                // Định dạng và hiển thị ngày đánh giá
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvReviewDate.text = dateFormat.format(Date(review.createdAt))

            }
            Log.d("ReviewProductAdapter", "Đã bind review của: ${review.userName} tại vị trí $position")
        } catch (e: Exception) {
            Log.e("ReviewProductAdapter", "Lỗi khi bind review ở vị trí $position: ${e.message}")
        }
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newReviews: List<ItemRecyclerViewOrderReviewProductUser>) {
        Log.d("ReviewProductAdapter", "updateData được gọi với ${newReviews.size} reviews")
        reviews = newReviews
        notifyDataSetChanged()
    }

}