package com.midterm22nh12.appbangiayonline.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Category

/**
 * Repository xử lý các hoạt động liên quan đến danh mục sản phẩm
 * Cung cấp các phương thức để truy xuất, thêm, sửa, xóa, và lọc danh mục từ Firebase Realtime Database
 */
class CategoryRepository {
    // Khởi tạo kết nối với Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()
    // Tham chiếu đến node "categories" trong database
    private val categoriesRef = database.getReference("categories")

    /**
     * Lấy danh sách tất cả danh mục
     * Sử dụng ValueEventListener để lắng nghe thay đổi dữ liệu theo thời gian thực
     *
     * @return LiveData<List<Category>> - đối tượng LiveData chứa danh sách danh mục,
     *         tự động cập nhật UI khi dữ liệu thay đổi
     */
    fun getCategories(): LiveData<List<Category>> {
        // Tạo MutableLiveData để lưu và trả về kết quả
        val categoriesLiveData = MutableLiveData<List<Category>>()

        // Đăng ký listener để lắng nghe thay đổi từ Firebase
        categoriesRef.addValueEventListener(object : ValueEventListener {
            // Được gọi khi dữ liệu thay đổi hoặc lần đầu đọc dữ liệu
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoriesList = mutableListOf<Category>()
                // Duyệt qua từng con của snapshot (mỗi con là một danh mục)
                for (categorySnapshot in snapshot.children) {
                    // Chuyển đổi dữ liệu từ snapshot thành đối tượng Category
                    val category = categorySnapshot.getValue(Category::class.java)
                    // Thêm vào danh sách nếu chuyển đổi thành công
                    category?.let { categoriesList.add(it) }
                }
                // Cập nhật giá trị cho LiveData
                categoriesLiveData.value = categoriesList
            }

            // Được gọi khi có lỗi xảy ra khi truy cập dữ liệu
            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi (có thể log lỗi hoặc thông báo)
            }
        })

        return categoriesLiveData
    }

    /**
     * Lấy thông tin chi tiết của một danh mục dựa trên ID
     * Sử dụng addListenerForSingleValueEvent để chỉ lấy dữ liệu một lần (không lắng nghe liên tục)
     *
     * @param categoryId ID của danh mục cần lấy thông tin
     * @return MutableLiveData<Category?> - đối tượng LiveData chứa thông tin danh mục,
     *         có thể là null nếu không tìm thấy
     */
    fun getCategoryById(categoryId: String): MutableLiveData<Category?> {
        // Tạo MutableLiveData để lưu và trả về kết quả
        val categoryLiveData = MutableLiveData<Category?>()

        // Truy vấn đến node con cụ thể dựa trên ID và chỉ lắng nghe một lần
        categoriesRef.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Chuyển đổi dữ liệu thành đối tượng Category
                val category = snapshot.getValue(Category::class.java)
                // Cập nhật giá trị cho LiveData
                categoryLiveData.value = category
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi (có thể log lỗi hoặc thông báo)
            }
        })

        return categoryLiveData
    }

    /**
     * Thêm danh mục mới vào database
     *
     * @param category Đối tượng Category chứa thông tin danh mục cần thêm
     * @param callback Hàm callback xử lý kết quả sau khi thêm
     *        - Tham số Boolean: true nếu thành công, false nếu thất bại
     *        - Tham số String?: null nếu thành công, chứa thông báo lỗi nếu thất bại
     */
    fun addCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        // Thêm danh mục vào database, sử dụng ID của category làm key
        categoriesRef.child(category.id).setValue(category)
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
     * Cập nhật thông tin danh mục
     *
     * @param category Đối tượng Category chứa thông tin cập nhật, ID phải trùng với danh mục cần cập nhật
     * @param callback Hàm callback xử lý kết quả sau khi cập nhật
     */
    fun updateCategory(category: Category, callback: (Boolean, String?) -> Unit) {
        // Cập nhật toàn bộ thông tin của danh mục (ghi đè node cũ)
        categoriesRef.child(category.id).setValue(category)
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
     * Xóa danh mục khỏi database
     *
     * @param categoryId ID của danh mục cần xóa
     * @param callback Hàm callback xử lý kết quả sau khi xóa
     */
    fun deleteCategory(categoryId: String, callback: (Boolean, String?) -> Unit) {
        // Xóa node danh mục dựa trên ID
        categoriesRef.child(categoryId).removeValue()
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
     * Lấy danh sách danh mục theo trạng thái hoạt động
     * Sử dụng truy vấn orderByChild để lọc theo một trường cụ thể
     *
     * @param active Trạng thái hoạt động cần lọc (true - đang hoạt động, false - không hoạt động)
     * @return LiveData<List<Category>> - đối tượng LiveData chứa danh sách danh mục đã lọc
     */
    fun getCategoriesByStatus(active: Boolean): LiveData<List<Category>> {
        // Tạo MutableLiveData để lưu và trả về kết quả
        val categoriesLiveData = MutableLiveData<List<Category>>()

        // Sử dụng orderByChild và equalTo để lọc dữ liệu theo trường "active"
        categoriesRef.orderByChild("active").equalTo(active).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoriesList = mutableListOf<Category>()
                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    category?.let { categoriesList.add(it) }
                }
                categoriesLiveData.value = categoriesList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi (có thể log lỗi hoặc thông báo)
            }
        })

        return categoriesLiveData
    }
}