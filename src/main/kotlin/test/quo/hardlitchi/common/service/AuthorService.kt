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
     * 
     * @param dto 著者作成用DTO
     * @return 作成された著者エンティティ
     * @throws DuplicateResourceException 同名の著者が既に存在する場合
     * @throws IllegalArgumentException 入力値が不正な場合
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
     * 
     * @param dto 著者更新用DTO
     * @return 更新された著者エンティティ
     * @throws ResourceNotFoundException 指定された著者が存在しない場合
     * @throws IllegalArgumentException 入力値が不正な場合
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
     * 
     * @param name 検索する著者名
     * @return 見つかった著者エンティティ、存在しない場合はnull
     */
    @Transactional(readOnly = true)
    fun findByName(name: String): Author? {
        return authorRepository.findByName(name)
    }

    /**
     * 全ての著者を取得する
     * 
     * @return 全著者のリスト（名前順でソート）
     */
    @Transactional(readOnly = true)
    fun findAll(): List<Author> {
        return authorRepository.findAll()
    }

    /**
     * 著者を削除する
     * 
     * @param name 削除する著者名
     * @throws ResourceNotFoundException 指定された著者が存在しない場合
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
     * 
     * @param name チェックする著者名
     * @return 存在する場合true、存在しない場合false
     */
    @Transactional(readOnly = true)
    fun existsByName(name: String): Boolean {
        return authorRepository.existsByName(name)
    }

    /**
     * 著者DTOのバリデーション
     * 
     * @param name 著者名
     * @param birthDate 生年月日
     * @throws IllegalArgumentException バリデーションエラーの場合
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
     * 
     * @return 変換された著者エンティティ
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