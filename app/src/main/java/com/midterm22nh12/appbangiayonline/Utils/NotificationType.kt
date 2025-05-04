package com.midterm22nh12.appbangiayonline.Utils

import com.midterm22nh12.appbangiayonline.R

// Tạo enum cho các loại thông báo:
enum class NotificationType(val keywords: List<String>, val iconResId: Int) {
    MESSAGE(listOf("tin nhắn", "message"), R.drawable.sms),
    PRODUCT(listOf("sản phẩm", "product"), R.drawable.btn_2),
    ACCOUNT(listOf("tài khoản", "account"), R.drawable.person),
    ORDER(listOf("đơn hàng", "order"), R.drawable.local_shipping),
    MAIL(listOf("mail", "email"), R.drawable.mail),
    SALE(listOf("sale", "khuyến mãi"), R.drawable.sale),
    FAVORITE(listOf("yêu thích", "favorite"), R.drawable.favorite);

    companion object {
        fun getByTitle(title: String): NotificationType {
            return entries.find { type ->
                type.keywords.any { keyword ->
                    title.contains(keyword, ignoreCase = true)
                }
            } ?: MAIL
        }
    }
}
