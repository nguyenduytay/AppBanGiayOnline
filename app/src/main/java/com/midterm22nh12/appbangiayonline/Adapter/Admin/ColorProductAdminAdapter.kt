package com.midterm22nh12.appbangiayonline.Adapter.Admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.midterm22nh12.appbangiayonline.R
import com.midterm22nh12.appbangiayonline.databinding.ItemColorProductAdminBinding
import com.midterm22nh12.appbangiayonline.model.Entity.Product.ProductColor

/**
 * Adapter để hiển thị và quản lý danh sách màu sắc sản phẩm trong giao diện Admin
 * Hỗ trợ các thao tác xem, thêm, sửa, xóa màu sắc sản phẩm
 *
 * @param context Context của ứng dụng, dùng để truy cập tài nguyên
 * @param colors Danh sách màu sắc sản phẩm cần hiển thị
 * @param onColorDeleted Callback khi một màu sắc bị xóa, nhận vị trí đã xóa làm tham số
 * @param onColorEdited Callback khi một màu sắc được sửa, nhận đối tượng màu sắc và vị trí làm tham số
 */
class ColorProductAdminAdapter(
    private val context: Context,
    private var colors: MutableList<ProductColor>,
    private val onColorDeleted: (Int) -> Unit,
    private val onColorEdited: (ProductColor, Int) -> Unit
) : RecyclerView.Adapter<ColorProductAdminAdapter.ViewHolder>() {

    /**
     * ViewHolder giữ các thành phần giao diện cho mỗi item màu sắc
     * @param binding Binding tới layout của item màu sắc
     */
    inner class ViewHolder(val binding: ItemColorProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Tạo mới ViewHolder khi RecyclerView cần một layout mới
     * @param parent ViewGroup cha chứa ViewHolder mới
     * @param viewType Loại view (không sử dụng trong trường hợp này)
     * @return ViewHolder mới được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemColorProductAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Gắn dữ liệu vào ViewHolder tại vị trí cụ thể
     * Hiển thị thông tin màu sắc và thiết lập các sự kiện click
     *
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
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

    /**
     * Trả về số lượng item trong danh sách
     * @return Số lượng màu sắc
     */
    override fun getItemCount() = colors.size

    /**
     * Cập nhật toàn bộ danh sách màu sắc
     * @param newColors Danh sách màu sắc mới
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateColors(newColors: List<ProductColor>) {
        colors.clear()
        colors.addAll(newColors)
        notifyDataSetChanged()
    }

    /**
     * Thêm một màu sắc mới vào danh sách
     * @param color Màu sắc cần thêm
     */
    fun addColor(color: ProductColor) {
        colors.add(color)
        notifyItemInserted(colors.size - 1)
    }

    /**
     * Cập nhật một màu sắc tại vị trí cụ thể
     * @param color Thông tin màu sắc mới
     * @param position Vị trí cần cập nhật
     */
    fun updateColor(color: ProductColor, position: Int) {
        if (position >= 0 && position < colors.size) {
            colors[position] = color
            notifyItemChanged(position)
        }
    }

    /**
     * Lấy danh sách tất cả các màu sắc hiện tại
     * @return Danh sách màu sắc
     */
    fun getColors(): List<ProductColor> = colors
}