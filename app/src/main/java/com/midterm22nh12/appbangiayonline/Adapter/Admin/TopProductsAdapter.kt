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

/**
 * Adapter hiển thị danh sách sản phẩm bán chạy nhất trên giao diện Admin Dashboard
 * Sắp xếp và hiển thị sản phẩm theo thứ hạng dựa trên số lượng bán ra hoặc doanh thu
 *
 * @param context Context để truy cập tài nguyên và dịch vụ của ứng dụng
 * @param topProducts Danh sách sản phẩm bán chạy cần hiển thị
 */
class TopProductsAdapter(
    private val context: Context,
    private var topProducts: List<TopProduct>
) : RecyclerView.Adapter<TopProductsAdapter.ViewHolder>() {

    /**
     * ViewHolder để lưu trữ các thành phần giao diện cho mỗi item sản phẩm bán chạy
     * @param binding Binding tới layout của item sản phẩm bán chạy
     */
    inner class ViewHolder(val binding: ItemTopProductBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Tạo một ViewHolder mới khi RecyclerView cần
     *
     * @param parent ViewGroup cha chứa ViewHolder mới
     * @param viewType Loại view (không sử dụng trong trường hợp này)
     * @return ViewHolder mới được tạo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    /**
     * Gắn dữ liệu vào ViewHolder tại vị trí cụ thể
     * Hiển thị thông tin sản phẩm bán chạy bao gồm tên, số lượng bán, doanh thu và thứ hạng
     * Thay đổi màu sắc thứ hạng dựa trên vị trí (3 vị trí đầu tiên có màu nổi bật)
     *
     * @param holder ViewHolder cần gắn dữ liệu
     * @param position Vị trí của item trong danh sách
     */
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

    /**
     * Trả về số lượng item trong danh sách sản phẩm bán chạy
     * @return Số lượng sản phẩm
     */
    override fun getItemCount(): Int = topProducts.size

    /**
     * Cập nhật danh sách sản phẩm bán chạy với dữ liệu mới
     * @param newProducts Danh sách sản phẩm bán chạy mới cần hiển thị
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newProducts: List<TopProduct>) {
        topProducts = newProducts
        notifyDataSetChanged()
    }
}