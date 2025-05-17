package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemColorProductAdminBinding
import com.midterm22nh12.appbangiayonline.databinding.ItemTopProductBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor

class ColorProductAdminAdapter(
    private val context: Context,
    private var colors: MutableList<ProductColor>,
    private val onColorDeleted: (Int) -> Unit,
    private val onColorEdited: (ProductColor, Int) -> Unit
) : RecyclerView.Adapter<ColorProductAdminAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemColorProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColorProductAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        val binding = holder.binding

        // Load ảnh
        Glide.with(context)
            .load(color.image)
            .placeholder(R.drawable.item_border_account)
            .error(R.drawable.item_border_account)
            .into(binding.ivColorPreview)

        binding.tvColorName.text = color.name
        binding.tvProductCode.text = color.productCode
        binding.tvStock.text = "SL: ${color.stock}"

        // Set trạng thái và màu sắc
        val statusText = when (color.status) {
            "available" -> "Có sẵn"
            "hidden" -> "Ẩn"
            "out_of_stock" -> "Hết hàng"
            "coming_soon" -> "Sắp có"
            else -> "Không xác định"
        }
        binding.tvColorStatus.text = statusText

        // Set màu cho trạng thái
        val statusColor = when (color.status) {
            "available" -> R.color.green
            "hidden" -> R.color.gray
            "out_of_stock" -> R.color.red
            "coming_soon" -> R.color.blue
            else -> R.color.black
        }
        binding.tvColorStatus.setTextColor(context.getColor(statusColor))

        // Xử lý sự kiện
        binding.ivEditColor.setOnClickListener {
            onColorEdited(color, position)
        }

        binding.ivDeleteColor.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa màu sắc này?")
                .setPositiveButton("Xóa") { _, _ ->
                    colors.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, colors.size)
                    onColorDeleted(position)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun getItemCount() = colors.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateColors(newColors: List<ProductColor>) {
        colors.clear()
        colors.addAll(newColors)
        notifyDataSetChanged()
    }

    fun addColor(color: ProductColor) {
        colors.add(color)
        notifyItemInserted(colors.size - 1)
    }

    fun updateColor(color: ProductColor, position: Int) {
        if (position >= 0 && position < colors.size) {
            colors[position] = color
            notifyItemChanged(position)
        }
    }

    fun getColors(): List<ProductColor> = colors
}