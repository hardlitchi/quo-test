package test.quo.hardlitchi.common.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.common.service.*
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AuthorServiceのテストクラス
 * TDD: Service層のビジネスロジックとトランザクション処理をテスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorServiceTest {

    @Autowired
    lateinit var authorService: AuthorService
    
    @Autowired
    lateinit var dslContext: DSLContext

    private lateinit var validAuthorDto: CreateAuthorDto
    private lateinit var invalidAuthorDto: CreateAuthorDto

    @BeforeEach
    fun setUp() {
        // テーブルをクリア（TRUNCATE CASCADEを使用して制約を回避）
        try {
            dslContext.execute("TRUNCATE TABLE publications, books, authors RESTART IDENTITY CASCADE")
        } catch (e: Exception) {
            // TRUNCATE が失敗した場合は個別削除（テストが空の状態で開始される場合）
            try {
                dslContext.deleteFrom(PUBLICATIONS).execute()
                dslContext.deleteFrom(BOOKS).execute()
                dslContext.deleteFrom(AUTHORS).execute()
            } catch (e2: Exception) {
                // どちらも失敗した場合はログを出力して続行
                println("Warning: Failed to clean tables: ${e2.message}")
            }
        }
        
        validAuthorDto = CreateAuthorDto(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        invalidAuthorDto = CreateAuthorDto(
            name = "",  // 空文字（バリデーションエラー）
            birthDate = LocalDate.now().plusDays(1)  // 未来の日付（バリデーションエラー）
        )
    }

    @Test
    @DisplayName("正常な著者を登録できる")
    fun canCreateValidAuthor() {
        // When
        val result = authorService.createAuthor(validAuthorDto)

        // Then
        assertThat(result.name).isEqualTo("夏目漱石")
        assertThat(result.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
        
        // DBに実際に保存されていることを確認
        val found = authorService.findByName("夏目漱石")
        assertThat(found).isNotNull
        assertThat(found!!.name).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("空文字の著者名で登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingAuthorWithEmptyName() {
        // Given
        val invalidDto = validAuthorDto.copy(name = "")

        // When & Then
        assertThatThrownBy {
            authorService.createAuthor(invalidDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("著者名は必須です")
    }

    @Test
    @DisplayName("未来の生年月日で登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingAuthorWithFutureBirthDate() {
        // Given
        val invalidDto = validAuthorDto.copy(birthDate = LocalDate.now().plusDays(1))

        // When & Then
        assertThatThrownBy {
            authorService.createAuthor(invalidDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("生年月日は現在の日付より過去である必要があります")
    }

    @Test
    @DisplayName("今日の日付の生年月日で登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingAuthorWithTodayBirthDate() {
        // Given: 今日の日付を生年月日として設定（境界値テスト）
        val invalidDto = validAuthorDto.copy(birthDate = LocalDate.now())

        // When & Then
        assertThatThrownBy {
            authorService.createAuthor(invalidDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("生年月日は現在の日付より過去である必要があります")
    }

    @Test
    @DisplayName("重複する著者名で登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingDuplicateAuthor() {
        // Given: 既に著者が登録済み
        authorService.createAuthor(validAuthorDto)

        // When & Then: 同じ名前の著者を再度登録しようとすると例外発生
        assertThatThrownBy {
            authorService.createAuthor(validAuthorDto)
        }.isInstanceOf(DuplicateResourceException::class.java)
         .hasMessageContaining("著者は既に存在します: 夏目漱石")
    }

    @Test
    @DisplayName("存在する著者を更新できる")
    fun canUpdateExistingAuthor() {
        // Given: 著者を登録
        authorService.createAuthor(validAuthorDto)
        val updateDto = UpdateAuthorDto(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 1, 5)  // 生年月日を変更
        )

        // When
        val result = authorService.updateAuthor(updateDto)

        // Then
        assertThat(result.birthDate).isEqualTo(LocalDate.of(1867, 1, 5))
        assertThat(result.name).isEqualTo("夏目漱石")
        
        // DBで確認
        val found = authorService.findByName("夏目漱石")
        assertThat(found!!.birthDate).isEqualTo(LocalDate.of(1867, 1, 5))
    }

    @Test
    @DisplayName("存在しない著者を更新しようとすると例外が発生する")
    fun throwsExceptionWhenUpdatingNonExistentAuthor() {
        // Given
        val updateDto = UpdateAuthorDto(
            name = "存在しない著者",
            birthDate = LocalDate.of(1900, 1, 1)
        )

        // When & Then
        assertThatThrownBy {
            authorService.updateAuthor(updateDto)
        }.isInstanceOf(ResourceNotFoundException::class.java)
         .hasMessageContaining("著者が見つかりません: 存在しない著者")
    }

    @Test
    @DisplayName("存在する著者を名前で検索できる")
    fun canFindExistingAuthorByName() {
        // Given
        authorService.createAuthor(validAuthorDto)

        // When
        val result = authorService.findByName("夏目漱石")

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo("夏目漱石")
        assertThat(result.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
    }

    @Test
    @DisplayName("存在しない著者を名前で検索するとnullが返される")
    fun returnsNullWhenSearchingForNonExistentAuthor() {
        // When
        val result = authorService.findByName("存在しない著者")

        // Then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("全ての著者を取得できる")
    fun canRetrieveAllAuthors() {
        // Given
        authorService.createAuthor(validAuthorDto)
        val author2Dto = CreateAuthorDto(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        authorService.createAuthor(author2Dto)

        // When
        val result = authorService.findAll()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder("夏目漱石", "芥川龍之介")
    }

    @Test
    @DisplayName("著者を削除できる")
    fun canDeleteAuthor() {
        // Given
        authorService.createAuthor(validAuthorDto)
        assertThat(authorService.existsByName("夏目漱石")).isTrue()

        // When
        authorService.deleteByName("夏目漱石")

        // Then
        assertThat(authorService.existsByName("夏目漱石")).isFalse()
        assertThat(authorService.findByName("夏目漱石")).isNull()
    }

    @Test
    @DisplayName("存在しない著者を削除しようとすると例外が発生する")
    fun throwsExceptionWhenDeletingNonExistentAuthor() {
        // When & Then
        assertThatThrownBy {
            authorService.deleteByName("存在しない著者")
        }.isInstanceOf(ResourceNotFoundException::class.java)
         .hasMessageContaining("著者が見つかりません: 存在しない著者")
    }

    @Test
    @DisplayName("著者の存在チェックができる")
    fun canCheckIfAuthorExists() {
        // Given
        authorService.createAuthor(validAuthorDto)

        // When & Then
        assertThat(authorService.existsByName("夏目漱石")).isTrue()
        assertThat(authorService.existsByName("存在しない著者")).isFalse()
    }

    @Test
    @DisplayName("トランザクションが正常にロールバックされる")
    fun transactionRollsBackProperly() {
        // Given
        authorService.createAuthor(validAuthorDto)
        
        // When & Then: 例外が発生した場合、既存データは変更されない
        assertThatThrownBy {
            authorService.createAuthor(validAuthorDto) // 重複エラー
        }.isInstanceOf(DuplicateResourceException::class.java)
        
        // 元のデータは変更されていないことを確認
        val found = authorService.findByName("夏目漱石")
        assertThat(found).isNotNull
        assertThat(found!!.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
    }
}