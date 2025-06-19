package test.quo.hardlitchi.common.entity

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 著者エンティティ
 * 
 * @property name 著者名（主キー）
 * @property birthDate 生年月日（現在の日付より過去であること）
 * @property createdAt 作成日時
 * @property createdBy 作成者
 * @property updatedAt 更新日時
 * @property updatedBy 更新者
 */
data class Author(
    val name: String,
    val birthDate: LocalDate,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val updatedAt: LocalDateTime,
    val updatedBy: String
) {
    init {
        require(name.isNotBlank()) { "著者名は必須です" }
        require(birthDate.isBefore(LocalDate.now())) { "生年月日は現在の日付より過去である必要があります" }
        require(createdBy.isNotBlank()) { "作成者は必須です" }
        require(updatedBy.isNotBlank()) { "更新者は必須です" }
    }
}