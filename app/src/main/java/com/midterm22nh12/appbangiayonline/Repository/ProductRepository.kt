package com.midterm22nh12.appbangiayonline.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.midterm22nh12.appbangiayonline.model.Entity.Product.Product
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
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "getProducts cancelled: ${error.message}")
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
                Log.e("ProductRepository", "getProductById cancelled: ${error.message}")
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
                    Log.e("ProductRepository", "getProductsByCategory cancelled: ${error.message}")
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
                    Log.e("ProductRepository", "getProductsByBrand cancelled: ${error.message}")
                }
            })
        return productsLiveData
    }

    fun searchProductsByName(query: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()
        val formattedQuery = query.lowercase(Locale.getDefault())
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null && product.name.lowercase(Locale.getDefault()).contains(formattedQuery)) {
                        results.add(product)
                    }
                }
                productsLiveData.value = results
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "searchProductsByName cancelled: ${error.message}")
            }
        })
        return productsLiveData
    }

    fun filterProductsByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()
        productsRef.orderByChild("price").startAt(minPrice).endAt(maxPrice)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val results = mutableListOf<Product>()
                    for (productSnapshot in snapshot.children) {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let { results.add(it) }
                    }
                    productsLiveData.value = results
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProductRepository", "filterProductsByPriceRange cancelled: ${error.message}")
                }
            })
        return productsLiveData
    }

    fun filterProductsByAvailableSize(size: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        if (it.sizes.any { s -> s.value == size && s.status == "available" }) {
                            results.add(it)
                        }
                    }
                }
                productsLiveData.value = results
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "filterProductsByAvailableSize cancelled: ${error.message}")
            }
        })
        return productsLiveData
    }

    fun filterProductsByAvailableColor(colorName: String): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        if (it.colors.any { c -> c.name == colorName && c.status == "available" && c.stock > 0 }) {
                            results.add(it)
                        }
                    }
                }
                productsLiveData.value = results
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "filterProductsByAvailableColor cancelled: ${error.message}")
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

    fun searchProductsWithMultipleCriteria(
        categoryId: String? = null,
        brandId: String? = null,
        nameQuery: String? = null
    ): LiveData<List<Product>> {
        val productsLiveData = MutableLiveData<List<Product>>()
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<Product>()
                val lowerCaseNameQuery = nameQuery?.lowercase(Locale.getDefault())

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        val matchesCategory = categoryId.isNullOrEmpty() || it.categoryId == categoryId
                        val matchesBrand = brandId.isNullOrEmpty() || it.brandId == brandId
                        val matchesName = lowerCaseNameQuery.isNullOrEmpty() || it.name.lowercase(Locale.getDefault()).contains(lowerCaseNameQuery)

                        if (matchesCategory && matchesBrand && matchesName) {
                            results.add(it)
                        }
                    }
                }
                productsLiveData.value = results
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "searchProductsWithMultipleCriteria cancelled: ${error.message}")
            }
        })
        return productsLiveData
    }
}
