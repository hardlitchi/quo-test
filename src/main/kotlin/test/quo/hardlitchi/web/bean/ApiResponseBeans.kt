package test.quo.hardlitchi.web.bean

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * API共通レスポンス形式
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val errors: List<FieldError>? = null
)

/**
 * フィールドエラー情報
 */
data class FieldError(
    val field: String,
    val message: String
)
