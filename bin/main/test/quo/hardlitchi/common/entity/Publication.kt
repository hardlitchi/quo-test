package test.quo.hardlitchi.common.entity

import java.time.LocalDateTime

/**
 * 出版エンティティ（書籍と著者の多対多関係を管理）
 * 
 * @property bookTitle 書籍タイトル（複合主キーの一部）
 * @property authorName 著者名（複合主キーの一部）
 * @property createdAt 作成日時
 * @property createdBy 作成者
 * @property updatedAt 更新日時
 * @property updatedBy 更新者
 */
data class Publication(
    val bookTitle: String,
    val authorName: String,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val updatedAt: LocalDateTime,
    val updatedBy: String
) {
    init {
        require(bookTitle.isNotBlank()) { "書籍タイトルは必須です" }
        require(authorName.isNotBlank()) { "著者名は必須です" }
        require(createdBy.isNotBlank()) { "作成者は必須です" }
        require(updatedBy.isNotBlank()) { "更新者は必須です" }
    }
}

/**
 * 複合主キーを表すデータクラス
 */
data class PublicationId(
    val bookTitle: String,
    val authorName: String
) {
    init {
        require(bookTitle.isNotBlank()) { "書籍タイトルは必須です" }
        require(authorName.isNotBlank()) { "著者名は必須です" }
    }
}