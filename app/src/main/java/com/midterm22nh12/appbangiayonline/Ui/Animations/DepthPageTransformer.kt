package com.midterm22nh12.appbangiayonline.Ui.Animations

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

/**
 * Hằng số xác định tỷ lệ thu nhỏ tối thiểu của trang khi chuyển đổi
 * Giá trị 0.75f nghĩa là trang sẽ thu nhỏ xuống còn 75% khi ở trạng thái chuyển đổi tối đa
 */
private const val MIN_SCALE = 0.75f

/**
 * Lớp PageTransformer tạo hiệu ứng chuyển trang 3D theo chiều sâu
 * Khi chuyển trang, trang hiện tại sẽ mờ dần và di chuyển ra sau
 * trong khi trang mới sẽ xuất hiện từ bên phải
 */
class DepthPageTransformer : ViewPager2.PageTransformer {

    /**
     * Áp dụng biến đổi cho trang khi người dùng cuộn ViewPager2
     *
     * @param view View của trang cần áp dụng hiệu ứng chuyển đổi
     * @param position Vị trí tương đối của trang [-1,1]:
     *                 0 khi trang ở chính giữa,
     *                 -1 khi trang ở bên trái hoàn toàn,
     *                 1 khi trang ở bên phải hoàn toàn
     */
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // Trang này nằm hoàn toàn bên ngoài màn hình bên trái
                    // Ẩn trang bằng cách đặt độ trong suốt = 0
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // Trang hiện tại hoặc đang chuyển sang trái
                    // Sử dụng hiệu ứng trượt mặc định, giữ nguyên kích thước và độ trong suốt
                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Trang đang chuyển từ phải sang trung tâm

                    // Làm mờ dần trang khi di chuyển từ phải vào trung tâm
                    alpha = 1 - position

                    // Chống lại hiệu ứng trượt mặc định bằng cách dịch chuyển sang trái
                    translationX = pageWidth * -position

                    // Di chuyển trang vào sau (giảm độ sâu Z) để tạo hiệu ứng 3D
                    translationZ = -1f

                    // Thu nhỏ trang (giữa MIN_SCALE và 1)
                    // Tỷ lệ thu nhỏ sẽ giảm dần từ 1 xuống MIN_SCALE khi position tăng từ 0 đến 1
                    val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position)))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // Trang này nằm hoàn toàn bên ngoài màn hình bên phải
                    // Ẩn trang bằng cách đặt độ trong suốt = 0
                    alpha = 0f
                }
            }
        }
    }
}