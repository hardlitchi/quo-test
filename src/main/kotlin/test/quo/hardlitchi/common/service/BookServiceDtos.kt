package test.quo.hardlitchi.common.service

import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.Publication
import test.quo.hardlitchi.common.entity.PublicationStatus
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 書籍作成用DTO
 */
data class CreateBookDto(
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val authors: List<String>,
    val createdBy: String = "system"
) {
    /**
     * DTOからBookエンティティに変換
     */
    fun toEntity(): Book {
        val now = LocalDateTime.now()
        return Book(
            title = title,
            price = price,
            publicationStatus = publicationStatus,
            createdAt = now,
            createdBy = createdBy,
            updatedAt = now,
            updatedBy = createdBy
        )
    }
    
    /**
     * 出版関係のエンティティリストを作成
     */
    fun toPublications(): List<Publication> {
        val now = LocalDateTime.now()
        return authors.map { authorName ->
            Publication(
                bookTitle = title,
                authorName = authorName,
                createdAt = now,
                createdBy = createdBy,
                updatedAt = now,
                updatedBy = createdBy
            )
        }
    }
}

/**
 * 書籍更新用DTO
 */
data class UpdateBookDto(
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val authors: List<String>,
    val updatedBy: String = "system"
) {
    /**
     * 既存のBookエンティティを更新
     */
    fun updateEntity(existingBook: Book): Book {
        return existingBook.copy(
            price = price,
            publicationStatus = publicationStatus,
            updatedBy = updatedBy,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 出版関係のエンティティリストを作成
     */
    fun toPublications(): List<Publication> {
        val now = LocalDateTime.now()
        return authors.map { authorName ->
            Publication(
                bookTitle = title,
                authorName = authorName,
                createdAt = now,
                createdBy = updatedBy,
                updatedAt = now,
                updatedBy = updatedBy
            )
        }
    }
}

/**
 * 書籍詳細情報DTO（著者情報含む）
 */
data class BookWithAuthorsDto(
    val book: Book,
    val authors: List<String>
)