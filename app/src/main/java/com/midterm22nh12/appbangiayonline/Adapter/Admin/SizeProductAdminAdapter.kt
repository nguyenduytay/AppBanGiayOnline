package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemSizeProductAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductSize

// Adapter cho Size
class SizeProductAdminAdapter(
    private val context: Context,
    private var sizes: MutableList<ProductSize>,
    private val onSizeDeleted: (Int) -> Unit,
    private val onSizeEdited: (ProductSize, Int) -> Unit
) : RecyclerView.Adapter<SizeProductAdminAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSizeProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSizeProductAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val size = sizes[position]
        val binding = holder.binding

        binding.tvSizeValue.text = size.value

        // Set trạng thái và màu sắc
        val statusText = when (size.status) {
            "available" -> "Có sẵn"
            "hidden" -> "Ẩn"
            "out_of_stock" -> "Hết hàng"
            "coming_soon" -> "Sắp có"
            else -> "Không xác định"
        }
        binding.tvSizeStatus.text = statusText

        // Set màu cho trạng thái
        val statusColor = when (size.status) {
            "available" -> R.color.green
            "hidden" -> R.color.gray
            "out_of_stock" -> R.color.red
            "coming_soon" -> R.color.blue
            else -> R.color.black
        }
        binding.tvSizeStatus.setTextColor(context.getColor(statusColor))

        // Xử lý sự kiện
        binding.ivEditSize.setOnClickListener {
            onSizeEdited(size, position)
        }

        binding.ivDeleteSize.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa kích thước này?")
                .setPositiveButton("Xóa") { _, _ ->
                    sizes.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, sizes.size)
                    onSizeDeleted(position)
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun getItemCount() = sizes.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSizes(newSizes: List<ProductSize>) {
        this.sizes.clear()
        this.sizes.addAll(newSizes)
        notifyDataSetChanged()
    }

    fun addSize(size: ProductSize) {
        sizes.add(size)
        notifyItemInserted(sizes.size - 1)
    }

    fun updateSize(size: ProductSize, position: Int) {
        if (position >= 0 && position < sizes.size) {
            sizes[position] = size
            notifyItemChanged(position)
        }
    }

    fun getSizes(): List<ProductSize> = sizes
}