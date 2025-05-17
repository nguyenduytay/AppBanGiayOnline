package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemTopProductBinding
import com.midterm22nh12.appbangiayonline.model.Item.TopProduct
import java.text.NumberFormat
import java.util.Locale

class TopProductsAdapter(
    private val context: Context,
    private var topProducts: List<TopProduct>
) : RecyclerView.Adapter<TopProductsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemTopProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = topProducts[position]
        val binding = holder.binding

        // Hiển thị thông tin sản phẩm
        binding.tvProductName.text = product.productName

        // Hiển thị số lượng bán
        binding.tvQuantitySold.text = "Đã bán: ${product.quantity}"

        // Hiển thị doanh thu
        val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        binding.tvRevenue.text = "${numberFormat.format(product.revenue)} đ"

        // Hiển thị hình ảnh sản phẩm
        if (product.productImage.isNotEmpty()) {
            Glide.with(binding.ivProductImage.context)
                .load(product.productImage)
                .placeholder(R.drawable.shoes)
                .error(R.drawable.shoes)
                .centerCrop()
        } else {
            binding.ivProductImage.setImageResource(R.drawable.shoes)
        }

        // Hiển thị ranking
        binding.tvRanking.text = "#${position + 1}"

        // Thiết lập màu sắc cho ranking dựa trên vị trí
        when (position) {
            0 -> binding.tvRanking.setTextColor(context.resources.getColor(R.color.status_shipping, null))
            1 -> binding.tvRanking.setTextColor(context.resources.getColor(R.color.status_processing, null))
            2 -> binding.tvRanking.setTextColor(context.resources.getColor(R.color.status_pending, null))
            else -> binding.tvRanking.setTextColor(context.resources.getColor(R.color.grey_500, null))
        }
    }

    override fun getItemCount(): Int = topProducts.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newProducts: List<TopProduct>) {
        topProducts = newProducts
        notifyDataSetChanged()
    }
}