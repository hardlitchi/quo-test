package test.quo.hardlitchi.common.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.common.repository.AuthorRepository
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 著者サービス
 * ビジネスロジックとトランザクション管理を担当
 */
@Service
@Transactional
class AuthorService(private val authorRepository: AuthorRepository) {

    /**
     * 著者を作成する
     */
    fun createAuthor(dto: CreateAuthorDto): Author {
        // バリデーション
        validateAuthorDto(dto.name, dto.birthDate)
        
        // 重複チェック
        if (authorRepository.existsByName(dto.name)) {
            throw DuplicateResourceException("著者は既に存在します: ${dto.name}")
        }
        
        // エンティティ作成
        val author = dto.toEntity()
        
        // 保存
        return authorRepository.insert(author)
    }

    /**
     * 著者を更新する
     */
    fun updateAuthor(dto: UpdateAuthorDto): Author {
        // バリデーション
        validateAuthorDto(dto.name, dto.birthDate)
        
        // 存在チェック
        val existingAuthor = authorRepository.findByName(dto.name)
            ?: throw ResourceNotFoundException("著者が見つかりません: ${dto.name}")
        
        // 更新対象エンティティ作成
        val updatedAuthor = existingAuthor.copy(
            birthDate = dto.birthDate,
            updatedBy = dto.updatedBy,
            updatedAt = LocalDateTime.now()
        )
        
        // 更新
        return authorRepository.update(updatedAuthor)
    }

    /**
     * 著者を名前で検索する
     */
    @Transactional(readOnly = true)
    fun findByName(name: String): Author? {
        return authorRepository.findByName(name)
    }

    /**
     * 全ての著者を取得する
     */
    @Transactional(readOnly = true)
    fun findAll(): List<Author> {
        return authorRepository.findAll()
    }

    /**
     * 著者を削除する
     */
    fun deleteByName(name: String) {
        // 存在チェック
        if (!authorRepository.existsByName(name)) {
            throw ResourceNotFoundException("著者が見つかりません: ${name}")
        }
        
        authorRepository.deleteByName(name)
    }

    /**
     * 著者が存在するかチェックする
     */
    @Transactional(readOnly = true)
    fun existsByName(name: String): Boolean {
        return authorRepository.existsByName(name)
    }

    /**
     * 著者DTOのバリデーション
     */
    private fun validateAuthorDto(name: String, birthDate: LocalDate) {
        if (name.isBlank()) {
            throw IllegalArgumentException("著者名は必須です")
        }
        
        if (birthDate.isAfter(LocalDate.now()) || birthDate.isEqual(LocalDate.now())) {
            throw IllegalArgumentException("生年月日は現在の日付より過去である必要があります")
        }
    }
}

/**
 * 著者作成用DTO
 */
data class CreateAuthorDto(
    val name: String,
    val birthDate: LocalDate,
    val createdBy: String = "system"
) {
    /**
     * DTOからEntityに変換
     */
    fun toEntity(): Author {
        val now = LocalDateTime.now()
        return Author(
            name = name,
            birthDate = birthDate,
            createdAt = now,
            createdBy = createdBy,
            updatedAt = now,
            updatedBy = createdBy
        )
    }
}

/**
 * 著者更新用DTO
 */
data class UpdateAuthorDto(
    val name: String,
    val birthDate: LocalDate,
    val updatedBy: String = "system"
)

/**
 * リソース未発見例外
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * 重複リソース例外
 */
class DuplicateResourceException(message: String) : RuntimeException(message)