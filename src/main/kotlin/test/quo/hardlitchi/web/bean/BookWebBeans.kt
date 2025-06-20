package test.quo.hardlitchi.web.bean

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.common.service.CreateBookDto
import test.quo.hardlitchi.common.service.UpdateBookDto
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.DecimalMin

/**
 * 書籍登録リクエスト
 */
data class CreateBookRequest(
    @field:NotBlank(message = "書籍タイトルは必須です")
    val title: String,
    
    @field:NotNull(message = "価格は必須です")
    @field:DecimalMin(value = "0", message = "価格は0以上である必要があります")
    val price: BigDecimal,
    
    @field:NotNull(message = "出版状況は必須です")
    val publicationStatus: PublicationStatus,
    
    @field:NotEmpty(message = "著者は最低1人必要です")
    val authors: List<String>
) {
    /**
     * WebリクエストからServiceのDTOに変換
     */
    fun toServiceDto(): CreateBookDto {
        return CreateBookDto(
            title = title,
            price = price,
            publicationStatus = publicationStatus,
            authors = authors
        )
    }
}

/**
 * 書籍更新リクエスト
 */
data class UpdateBookRequest(
    @field:NotNull(message = "価格は必須です")
    @field:DecimalMin(value = "0", message = "価格は0以上である必要があります")
    val price: BigDecimal,
    
    @field:NotNull(message = "出版状況は必須です")
    val publicationStatus: PublicationStatus,
    
    @field:NotEmpty(message = "著者は最低1人必要です")
    val authors: List<String>
) {
    /**
     * WebリクエストからServiceのDTOに変換
     */
    fun toServiceDto(title: String): UpdateBookDto {
        return UpdateBookDto(
            title = title,
            price = price,
            publicationStatus = publicationStatus,
            authors = authors
        )
    }
}

/**
 * 書籍レスポンス（著者情報含む）
 */
data class BookResponse(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val title: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val price: BigDecimal,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val publicationStatus: PublicationStatus,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val authors: List<String>,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * BookエンティティとauthorsからBookResponseを生成
         */
        fun from(book: Book, authors: List<String>): BookResponse {
            return BookResponse(
                title = book.title,
                price = book.price,
                publicationStatus = book.publicationStatus,
                authors = authors.sorted(),
                createdAt = book.createdAt,
                updatedAt = book.updatedAt
            )
        }
    }
}