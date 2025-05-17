package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewConfirmation
import com.midterm22nh12.appbangiayonline.model.Item.ItemRecyclerViewProductHomeUser
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Repository chịu trách nhiệm quản lý tất cả các thao tác liên quan đến dữ liệu sản phẩm
 * từ Firebase Realtime Database
 */
class ProductRepository {
    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")

    /**
     * Lấy tất cả sản phẩm từ Firebase Realtime Database
     * @return LiveData<List<Product>> Danh sách sản phẩm được cập nhật realtime
     */
    fun getProducts(): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let { productsList.add(it) }
                }
                productsLiveData.value = productsList
                Log.d("ProductRepository", "onCancelled: " + productsList.joinToString {
                    it.name
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ProductRepository", "onCancelled: ")
            }
        })

        return productsLiveData
    }

    /**
     * Lấy sản phẩm dựa trên ID
     * @param productId ID của sản phẩm cần lấy
     * @return MutableLiveData<Product?> Thông tin chi tiết của sản phẩm hoặc null nếu không tìm thấy
     */
    fun getProductById(productId: String): MutableLiveData<Product?> {
        val productLiveData = MutableLiveData<Product?>()

        productsRef.child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = snapshot.getValue(Product::class.java)
                productLiveData.value = product
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return productLiveData
    }

    /**
     * Lấy danh sách sản phẩm thuộc một danh mục cụ thể
     * @param categoryId ID của danh mục cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm thuộc danh mục
     */
    fun getProductsByCategory(categoryId: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        productsRef.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productsList = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { productsList.add(it) }
                    }
                    productsLiveData.value = productsList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi
                }
            })

        return productsLiveData
    }

    /**
     * Lấy danh sách sản phẩm thuộc một thương hiệu cụ thể
     * @param brandId ID của thương hiệu cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm thuộc thương hiệu
     */
    fun getProductsByBrand(brandId: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        productsRef.orderByChild("brandId").equalTo(brandId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productsList = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { productsList.add(it) }
                    }
                    productsLiveData.value = productsList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi
                }
            })

        return productsLiveData
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     * @param query Chuỗi tìm kiếm cho tên sản phẩm
     * @return LiveData<List<Product>> Danh sách sản phẩm có tên phù hợp
     */
    fun searchProductsByName(query: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        productsRef.orderByChild("name").startAt(query).endAt(query + "\uf8ff")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productsList = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { productsList.add(it) }
                    }
                    productsLiveData.value = productsList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi
                }
            })

        return productsLiveData
    }

    /**
     * Lọc sản phẩm theo khoảng giá
     * @param minPrice Giá tối thiểu
     * @param maxPrice Giá tối đa
     * @return LiveData<List<Product>> Danh sách sản phẩm có giá trong khoảng chỉ định
     */
    fun filterProductsByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        productsRef.orderByChild("price").startAt(minPrice).endAt(maxPrice)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productsList = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { productsList.add(it) }
                    }
                    productsLiveData.value = productsList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi
                }
            })

        return productsLiveData
    }

    /**
     * Lọc sản phẩm theo kích cỡ có sẵn
     * @param size Kích cỡ cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm có kích cỡ yêu cầu và trạng thái "available"
     */
    fun filterProductsByAvailableSize(size: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        // This is more complex and requires client-side filtering
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        // Check if product has the requested size with available status
                        if (it.sizes.any { s -> s.value == size && s.status == "available" }) {
                            productsList.add(it)
                        }
                    }
                }
                productsLiveData.value = productsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return productsLiveData
    }

    /**
     * Lọc sản phẩm theo màu sắc có sẵn
     * @param colorName Tên màu cần lọc
     * @return LiveData<List<Product>> Danh sách sản phẩm có màu yêu cầu, trạng thái "available" và còn trong kho
     */
    fun filterProductsByAvailableColor(colorName: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        // Check if product has the requested color with available status
                        if (it.colors.any { c -> c.name == colorName && c.status == "available" && c.stock > 0 }) {
                            productsList.add(it)
                        }
                    }
                }
                productsLiveData.value = productsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi
            }
        })

        return productsLiveData
    }

    /**
     * Thêm sản phẩm mới vào database
     * @param product Đối tượng Product chứa thông tin sản phẩm mới
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun addProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        productsRef.child(product.id).setValue(product)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Cập nhật thông tin sản phẩm
     * @param product Đối tượng Product chứa thông tin cập nhật
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun updateProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        productsRef.child(product.id).setValue(product)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Xóa một sản phẩm khỏi database
     * @param productId ID của sản phẩm cần xóa
     * @param callback Hàm callback trả về kết quả thành công/thất bại và thông báo lỗi (nếu có)
     */
    fun deleteProduct(productId: String, callback: (Boolean, String?) -> Unit) {
        productsRef.child(productId).removeValue()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    /**
     * Tìm kiếm sản phẩm với nhiều tiêu chí kết hợp
     * Tìm kiếm tên sản phẩm không phân biệt hoa thường
     *
     * @param categoryId ID của loại sản phẩm (null nếu không sử dụng tiêu chí này)
     * @param brandId ID của thương hiệu (null nếu không sử dụng tiêu chí này)
     * @param nameQuery Chuỗi con trong tên sản phẩm (null nếu không sử dụng tiêu chí này)
     * @return LiveData<List<Product>> Danh sách sản phẩm phù hợp
     */
    fun searchProductsWithMultipleCriteria(
        categoryId: String? = null,
        brandId: String? = null,
        nameQuery: String? = null
    ): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()

        // Lấy tất cả sản phẩm và lọc ở client
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = mutableListOf<Product>()

                // Chuyển nameQuery sang chữ thường nếu có
                val lowerCaseNameQuery = nameQuery?.lowercase(Locale.getDefault())

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        // Kiểm tra từng tiêu chí
                        var matchesCategory = true
                        var matchesBrand = true
                        var matchesName = true

                        // Kiểm tra loại sản phẩm nếu được chỉ định
                        if (!categoryId.isNullOrEmpty()) {
                            matchesCategory = (it.categoryId == categoryId)
                        }

                        // Kiểm tra thương hiệu nếu được chỉ định
                        if (!brandId.isNullOrEmpty()) {
                            matchesBrand = (it.brandId == brandId)
                        }

                        // Kiểm tra tên sản phẩm nếu được chỉ định, không phân biệt hoa thường
                        if (!lowerCaseNameQuery.isNullOrEmpty()) {
                            matchesName = it.name.lowercase(Locale.getDefault())
                                .contains(lowerCaseNameQuery)
                        }

                        // Chỉ thêm vào danh sách kết quả nếu thỏa mãn tất cả điều kiện áp dụng
                        if (matchesCategory && matchesBrand && matchesName) {
                            productsList.add(it)
                        }
                    }
                }

                productsLiveData.value = productsList
                Log.d("ProductRepository", "Found ${productsList.size} products matching criteria")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "Search cancelled: ${error.message}")
            }
        })

        return productsLiveData
    }

    /**
     * Lấy thông tin chi tiết sản phẩm từ ItemRecyclerViewConfirmation
     * @param confirmation Thông tin confirmation cần chuyển đổi
     * @return Thông tin chi tiết sản phẩm hoặc null nếu không tìm thấy
     */
    suspend fun getProductHomeUserFromConfirmation(confirmation: ItemRecyclerViewConfirmation): ItemRecyclerViewProductHomeUser? {
        try {
            // Nếu productId null thì trả về null
            val productId = confirmation.productId ?: return null

            // Truy vấn Firebase để lấy thông tin sản phẩm
            val productSnapshot = productsRef.child(productId).get().await()

            if (productSnapshot.exists()) {
                val product = productSnapshot.getValue(Product::class.java)

                if (product != null) {
                    // Chuyển đổi từ Product sang ItemRecyclerViewProductHomeUser
                    return ItemRecyclerViewProductHomeUser(
                        id = product.id,
                        brandId = product.brandId,
                        categoryId = product.categoryId,
                        name = product.name,
                        price = product.price,
                        rating = product.rating,
                        description = product.description,
                        sizes = product.sizes,
                        colors = product.colors
                    )
                }
            }

            // Trả về null nếu không tìm thấy sản phẩm
            return null
        } catch (e: Exception) {
            Log.e("ProductRepository", "Lỗi khi lấy thông tin sản phẩm: ${e.message}")
            return null
        }
    }
}