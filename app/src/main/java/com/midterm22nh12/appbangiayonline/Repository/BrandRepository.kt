package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Brand

/**
 * Repository xử lý các hoạt động liên quan đến thương hiệu sản phẩm
 * Cung cấp các phương thức để truy xuất, thêm, sửa, xóa thương hiệu từ Firebase Realtime Database
 */
class BrandRepository {
    // Khởi tạo kết nối đến Firebase Database
    private val database = FirebaseDatabase.getInstance()
    // Tham chiếu đến node "brands" trong database
    private val brandsRef = database.getReference("brands")

    /**
     * Lấy danh sách tất cả các thương hiệu
     * Sử dụng ValueEventListener để lắng nghe thay đổi dữ liệu theo thời gian thực
     * @return LiveData chứa danh sách các đối tượng Brand, tự động cập nhật khi dữ liệu thay đổi
     */
    fun getBrands(): LiveData<List<Brand>> {
        // Tạo LiveData để trả về cho ViewModel
        val brandsLiveData = MutableLiveData<List<Brand>>()

        // Đăng ký listener để lắng nghe dữ liệu từ Firebase
        brandsRef.addValueEventListener(object : ValueEventListener {
            // Được gọi khi dữ liệu thay đổi hoặc khi lần đầu đọc dữ liệu
            override fun onDataChange(snapshot: DataSnapshot) {
                val brandsList = mutableListOf<Brand>()
                // Duyệt qua từng con của snapshot (mỗi con là một thương hiệu)
                for (brandSnapshot in snapshot.children) {
                    // Chuyển đổi dữ liệu từ snapshot thành đối tượng Brand
                    val brand = brandSnapshot.getValue(Brand::class.java)
                    // Thêm vào danh sách nếu chuyển đổi thành công
                    brand?.let { brandsList.add(it) }
                }
                // Cập nhật giá trị cho LiveData
                brandsLiveData.value = brandsList
            }

            // Được gọi khi có lỗi xảy ra khi truy cập dữ liệu
            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi (có thể log lỗi hoặc thông báo)
            }
        })

        return brandsLiveData
    }

    /**
     * Lấy thông tin chi tiết của một thương hiệu dựa trên ID
     * Sử dụng addListenerForSingleValueEvent để chỉ lấy dữ liệu một lần
     * @param brandId ID của thương hiệu cần lấy thông tin
     * @return MutableLiveData chứa đối tượng Brand hoặc null nếu không tìm thấy
     */
    fun getBrandById(brandId: String): MutableLiveData<Brand?> {
        // Tạo LiveData để trả về cho ViewModel
        val brandLiveData = MutableLiveData<Brand?>()

        // Truy vấn đến node con cụ thể dựa trên ID và chỉ lắng nghe một lần
        brandsRef.child(brandId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chuyển đổi dữ liệu thành đối tượng Brand
                val brand = snapshot.getValue(Brand::class.java)
                // Cập nhật giá trị cho LiveData
                brandLiveData.value = brand
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return brandLiveData
    }

    /**
     * Thêm một thương hiệu mới vào database
     * @param brand Đối tượng Brand chứa thông tin thương hiệu cần thêm
     * @param callback Hàm callback xử lý kết quả thành công/thất bại
     *        - Tham số Boolean: true nếu thành công, false nếu thất bại
     *        - Tham số String?: null nếu thành công, chứa thông báo lỗi nếu thất bại
     */
    fun addBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        // Thêm thương hiệu vào database, sử dụng ID của brand làm key
        brandsRef.child(brand.id).setValue(brand)
            .addOnSuccessListener {
                // Gọi callback với kết quả thành công
                callback(true, null)
            }
            .addOnFailureListener { e ->
                // Gọi callback với kết quả thất bại và thông báo lỗi
                callback(false, e.message)
            }
    }

    /**
     * Cập nhật thông tin thương hiệu
     * @param brand Đối tượng Brand chứa thông tin cập nhật, ID phải trùng với thương hiệu cần cập nhật
     * @param callback Hàm callback xử lý kết quả thành công/thất bại
     */
    fun updateBrand(brand: Brand, callback: (Boolean, String?) -> Unit) {
        // Cập nhật toàn bộ thông tin của thương hiệu (ghi đè node cũ)
        brandsRef.child(brand.id).setValue(brand)
            .addOnSuccessListener {
                // Gọi callback với kết quả thành công
                callback(true, null)
            }
            .addOnFailureListener { e ->
                // Gọi callback với kết quả thất bại và thông báo lỗi
                callback(false, e.message)
            }
    }

    /**
     * Xóa thương hiệu khỏi database
     * @param brandId ID của thương hiệu cần xóa
     * @param callback Hàm callback xử lý kết quả thành công/thất bại
     */
    fun deleteBrand(brandId: String, callback: (Boolean, String?) -> Unit) {
        // Xóa node thương hiệu dựa trên ID
        brandsRef.child(brandId).removeValue()
            .addOnSuccessListener {
                // Gọi callback với kết quả thành công
                callback(true, null)
            }
            .addOnFailureListener { e ->
                // Gọi callback với kết quả thất bại và thông báo lỗi
                callback(false, e.message)
            }
    }
}