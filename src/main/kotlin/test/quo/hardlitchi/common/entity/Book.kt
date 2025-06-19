package test.quo.hardlitchi.common.entity

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 書籍エンティティ
 * 
 * @property title 書籍タイトル（主キー）
 * @property price 価格（0以上であること）
 * @property publicationStatus 出版状況
 * @property createdAt 作成日時
 * @property createdBy 作成者
 * @property updatedAt 更新日時
 * @property updatedBy 更新者
 */
data class Book(
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val updatedAt: LocalDateTime,
    val updatedBy: String
) {
    init {
        require(title.isNotBlank()) { "書籍タイトルは必須です" }
        require(price >= BigDecimal.ZERO) { "価格は0以上である必要があります" }
        require(createdBy.isNotBlank()) { "作成者は必須です" }
        require(updatedBy.isNotBlank()) { "更新者は必須です" }
    }
    
    /**
     * 出版状況を変更可能かチェックする
     * 
     * @param newStatus 新しい出版状況
     * @return 変更可能な場合true
     */
    fun canChangeStatusTo(newStatus: PublicationStatus): Boolean {
        // 出版済みの書籍を未出版に戻すことはできない
        return !(publicationStatus == PublicationStatus.PUBLISHED && newStatus == PublicationStatus.UNPUBLISHED)
    }
}

/**
 * 出版状況の列挙型
 */
enum class PublicationStatus {
    UNPUBLISHED,  // 未出版
    PUBLISHED     // 出版済み
}