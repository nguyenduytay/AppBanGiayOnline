package com.midterm22nh12.appbangiayonline.Utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import java.io.IOException
import java.util.*

object LocationUtil {
    private const val PERMISSION_ID = 1001
    @SuppressLint("StaticFieldLeak")
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    /**
     * Hàm để lấy địa chỉ hiện tại và gán vào EditText
     * @param fragment Fragment chứa EditText
     * @param editText EditText sẽ hiển thị địa chỉ
     * @param useDetailedAddress true nếu muốn địa chỉ chi tiết, false nếu chỉ muốn thông tin ngắn gọn
     * @param onAddressReceived callback function sẽ được gọi sau khi lấy được địa chỉ thành công
     */
    @SuppressLint("SetTextI18n")
    fun getAddressToTextView(
        fragment: Fragment,
        editText: EditText,
        useDetailedAddress: Boolean = true,
        onAddressReceived: ((String) -> Unit)? = null
    ) {
        val context = fragment.requireContext()

        // Khởi tạo FusedLocationClient nếu chưa có
        if (!::fusedLocationClient.isInitialized) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }

        // Kiểm tra và yêu cầu quyền nếu cần
        if (!checkPermissions(context)) {
            requestPermissions(fragment)
            editText.setText("Vui lòng cấp quyền vị trí")
            return
        }

        // Kiểm tra GPS đã bật chưa
        if (!isLocationEnabled(context)) {
            editText.setText("Vui lòng bật dịch vụ định vị (GPS)")
            Toast.makeText(context, "Vui lòng bật dịch vụ định vị (GPS)", Toast.LENGTH_LONG).show()
            return
        }

        // Thông báo đang lấy vị trí
        editText.setText("Đang lấy vị trí hiện tại...")

        try {
            // Kiểm tra quyền truy cập một lần nữa để tránh cảnh báo
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                editText.setText("Không có quyền truy cập vị trí")
                return
            }

            // Lấy vị trí hiện tại
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Chuyển đổi vị trí thành địa chỉ
                    getAddressFromLocation(context, location, editText, useDetailedAddress, onAddressReceived)
                } else {
                    // Nếu lastLocation là null, cần yêu cầu cập nhật vị trí mới
                    createLocationRequest()
                    createLocationCallback(context, editText, useDetailedAddress, onAddressReceived)
                    requestNewLocationData(context)
                }
            }.addOnFailureListener { e ->
                editText.setText("Lỗi khi lấy vị trí: ${e.message}")
            }
        } catch (e: Exception) {
            editText.setText("Lỗi: ${e.message}")
        }
    }

    // Kiểm tra quyền
    private fun checkPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Yêu cầu quyền
    private fun requestPermissions(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    // Kiểm tra GPS đã bật chưa
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Tạo yêu cầu vị trí
    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // Tạo callback cho cập nhật vị trí
    private fun createLocationCallback(
        context: Context,
        editText: EditText,
        useDetailedAddress: Boolean,
        onAddressReceived: ((String) -> Unit)?
    ) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                    getAddressFromLocation(context, it, editText, useDetailedAddress, onAddressReceived)
                    // Ngừng cập nhật vị trí sau khi nhận được
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }
    }

    // Yêu cầu cập nhật vị trí mới
    private fun requestNewLocationData(context: Context) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Chuyển đổi vị trí thành địa chỉ
    @SuppressLint("SetTextI18n")
    private fun getAddressFromLocation(
        context: Context,
        location: Location,
        editText: EditText,
        useDetailedAddress: Boolean,
        onAddressReceived: ((String) -> Unit)?
    ) {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            @Suppress("DEPRECATION")  // Để tương thích với các phiên bản Android cũ hơn
            val addresses: List<Address>? = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                var addressText = ""

                if (useDetailedAddress) {
                    // Địa chỉ chi tiết
                    addressText = buildString {
                        for (i in 0..address.maxAddressLineIndex) {
                            append(address.getAddressLine(i))
                            if (i < address.maxAddressLineIndex) {
                                append("\n")
                            }
                        }
                    }
                    editText.setText(addressText)
                } else {
                    // Địa chỉ ngắn gọn
                    addressText = buildString {
                        val featureName = address.featureName
                        val thoroughfare = address.thoroughfare
                        val subLocality = address.subLocality
                        val locality = address.locality
                        val adminArea = address.adminArea
                        val countryName = address.countryName

                        if (!featureName.isNullOrEmpty() && featureName != thoroughfare) {
                            append(featureName).append(", ")
                        }

                        if (!thoroughfare.isNullOrEmpty()) {
                            append(thoroughfare).append(", ")
                        }

                        if (!subLocality.isNullOrEmpty()) {
                            append(subLocality).append(", ")
                        }

                        if (!locality.isNullOrEmpty()) {
                            append(locality).append(", ")
                        }

                        if (!adminArea.isNullOrEmpty()) {
                            append(adminArea).append(", ")
                        }

                        if (!countryName.isNullOrEmpty()) {
                            append(countryName)
                        }
                    }
                    editText.setText(addressText)
                }

                // Gọi callback với địa chỉ đã lấy được
                onAddressReceived?.invoke(addressText)
            } else {
                editText.setText("Không tìm thấy địa chỉ")
            }
        } catch (e: IOException) {
            editText.setText("Lỗi khi lấy địa chỉ: ${e.message}")
            e.printStackTrace()
        }
    }

    // Ngừng cập nhật vị trí (gọi trong onPause của Fragment hoặc Activity)
    fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}