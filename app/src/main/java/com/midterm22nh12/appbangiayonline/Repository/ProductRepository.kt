package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product
import java.util.Locale

class ProductRepository {
    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")

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

    // Filter products by available sizes
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

    // Filter products by available colors
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

    fun addProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        productsRef.child(product.id).setValue(product)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun updateProduct(product: Product, callback: (Boolean, String?) -> Unit) {
        productsRef.child(product.id).setValue(product)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

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
}