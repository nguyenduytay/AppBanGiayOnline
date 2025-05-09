package com.midterm22nh12.appbangiayonline.Adapter.User

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemImageProductOrderUserBinding

class ProductImagePagerAdapter(
    private var imageList: List<String> = emptyList()
) : RecyclerView.Adapter<ProductImagePagerAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: ItemImageProductOrderUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageProductOrderUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun getItemCount() = imageList.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]

        // Tải ảnh bằng Glide
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_close_24)
            .error(R.drawable.baseline_close_24)
            .into(holder.binding.ivProductImageOrderUser)
    }

    // Phương thức cập nhật dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newImages: List<String>) {
        imageList = newImages
        notifyDataSetChanged()
    }
}