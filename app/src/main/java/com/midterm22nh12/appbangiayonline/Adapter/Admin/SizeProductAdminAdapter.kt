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

/**
 * Adapter để hiển thị và quản lý danh sách kích thước sản phẩm trong giao diện Admin
 * Hỗ trợ các thao tác xem, thêm, sửa, xóa kích thước sản phẩm
 *
 * @param context Context để truy cập tài nguyên và dịch vụ
 * @param sizes Danh sách kích thước sản phẩm cần hiển thị
 * @param onSizeDeleted Callback khi một kích thước bị xóa, nhận vị trí đã xóa làm tham số
 * @param onSizeEdited Callback khi một kích thước được sửa, nhận đối tượng kích thước và vị trí làm tham số
 */
class SizeProductAdminAdapter(
    private val context: Context,
    private var sizes: MutableList<ProductSize>,
    private val onSizeDeleted: (Int) -> Unit,
    private val onSizeEdited: (ProductSize, Int) -> Unit
) : RecyclerView.Adapter<SizeProductAdminAdapter.ViewHolder>() {

    /**
     * ViewHolder giữ các thành phần giao diện cho mỗi item kích thước
     * @param binding Binding tới layout của item kích thước
     */
    inner class ViewHolder(val binding: ItemSizeProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Tạo mới ViewHolder khi RecyclerView cần một layout mới
     * @param parent ViewGroup cha chứa ViewHolder mới
     * @param viewType Loại view (không sử dụng trong trường hợp này)
     * @return ViewHolder mới được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSizeProductAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Gắn dữ liệu vào ViewHolder tại vị trí cụ thể
     * Hiển thị thông tin kích thước và thiết lập các sự kiện click
     *
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
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

    /**
     * Trả về số lượng item trong danh sách
     * @return Số lượng kích thước
     */
    override fun getItemCount() = sizes.size

    /**
     * Cập nhật toàn bộ danh sách kích thước
     * @param newSizes Danh sách kích thước mới
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateSizes(newSizes: List<ProductSize>) {
        this.sizes.clear()
        this.sizes.addAll(newSizes)
        notifyDataSetChanged()
    }

    /**
     * Thêm một kích thước mới vào danh sách
     * @param size Kích thước cần thêm
     */
    fun addSize(size: ProductSize) {
        sizes.add(size)
        notifyItemInserted(sizes.size - 1)
    }

    /**
     * Cập nhật một kích thước tại vị trí cụ thể
     * @param size Thông tin kích thước mới
     * @param position Vị trí cần cập nhật
     */
    fun updateSize(size: ProductSize, position: Int) {
        if (position >= 0 && position < sizes.size) {
            sizes[position] = size
            notifyItemChanged(position)
        }
    }

    /**
     * Lấy danh sách tất cả các kích thước hiện tại
     * @return Danh sách kích thước
     */
    fun getSizes(): List<ProductSize> = sizes
}