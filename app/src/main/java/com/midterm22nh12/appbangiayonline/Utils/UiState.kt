package com.midterm22nh12.appbangiayonline.Utils

/**
 * Lớp đại diện cho trạng thái UI khi tương tác với Repository
 */
sealed class UiState<out T> {
    /**
     * Trạng thái đang tải dữ liệu
     */
    object Loading : UiState<Nothing>()

    /**
     * Trạng thái tải dữ liệu thành công
     * @param data Dữ liệu trả về
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Trạng thái tải dữ liệu thất bại
     * @param message Thông báo lỗi
     */
    data class Error(val message: String) : UiState<Nothing>()
}