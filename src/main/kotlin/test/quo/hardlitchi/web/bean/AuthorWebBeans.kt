package test.quo.hardlitchi.web.bean

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import test.quo.hardlitchi.common.service.CreateAuthorDto
import test.quo.hardlitchi.common.service.UpdateAuthorDto
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past

/**
 * 著者登録リクエスト
 */
data class CreateAuthorRequest(
    @field:NotBlank(message = "著者名は必須です")
    val name: String,
    
    @field:NotNull(message = "生年月日は必須です")
    @field:Past(message = "生年月日は現在の日付より過去である必要があります")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate
) {
    /**
     * WebリクエストからServiceのDTOに変換
     */
    fun toServiceDto(): CreateAuthorDto {
        return CreateAuthorDto(
            name = name,
            birthDate = birthDate
        )
    }
}

/**
 * 著者更新リクエスト
 */
data class UpdateAuthorRequest(
    @field:NotNull(message = "生年月日は必須です")
    @field:Past(message = "生年月日は現在の日付より過去である必要があります")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate
) {
    /**
     * WebリクエストからServiceのDTOに変換
     */
    fun toServiceDto(name: String): UpdateAuthorDto {
        return UpdateAuthorDto(
            name = name,
            birthDate = birthDate
        )
    }
}

/**
 * 著者レスポンス
 */
data class AuthorResponse(
    val name: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val birthDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val updatedAt: LocalDateTime
)

/**
 * API共通レスポンス形式
 */
data class ApiResponse<T>(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val success: Boolean,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: T?,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val message: String?,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val errors: List<FieldError>? = null
)

/**
 * フィールドエラー情報
 */
data class FieldError(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val field: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val message: String
)