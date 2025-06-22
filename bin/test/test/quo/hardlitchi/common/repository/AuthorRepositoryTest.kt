package test.quo.hardlitchi.common.repository

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
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AuthorRepositoryのテストクラス
 * TDD: Test Driven Development
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthorRepositoryTest {

    @Autowired
    lateinit var authorRepository: AuthorRepository
    
    @Autowired
    lateinit var dslContext: DSLContext

    private lateinit var testAuthor: Author

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
        
        testAuthor = Author(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.now(),
            createdBy = "test",
            updatedAt = LocalDateTime.now(),
            updatedBy = "test"
        )
    }

    @Test
    @DisplayName("著者を正常に登録できる")
    fun canInsertAuthorSuccessfully() {
        // When
        val result = authorRepository.insert(testAuthor)

        // Then
        assertThat(result.name).isEqualTo("夏目漱石")
        assertThat(result.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
        
        // 実際にDBに保存されたかを確認
        val found = authorRepository.findByName("夏目漱石")
        assertThat(found).isNotNull
        assertThat(found!!.name).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("重複する著者名で登録しようとすると例外が発生する")
    fun throwsExceptionWhenInsertingDuplicateAuthorName() {
        // Given: 既に著者が登録済み
        authorRepository.insert(testAuthor)

        // When & Then: 同じ名前の著者を再度登録しようとすると例外発生
        assertThatThrownBy {
            authorRepository.insert(testAuthor)
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    @DisplayName("存在する著者を名前で検索できる")
    fun canFindExistingAuthorByName() {
        // Given
        authorRepository.insert(testAuthor)

        // When
        val result = authorRepository.findByName("夏目漱石")

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.name).isEqualTo("夏目漱石")
        assertThat(result.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
    }

    @Test
    @DisplayName("存在しない著者を名前で検索するとnullが返される")
    fun returnsNullWhenSearchingForNonExistentAuthor() {
        // When
        val result = authorRepository.findByName("存在しない著者")

        // Then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("著者が存在するかチェックできる")
    fun canCheckIfAuthorExists() {
        // Given
        authorRepository.insert(testAuthor)

        // When & Then
        assertThat(authorRepository.existsByName("夏目漱石")).isTrue()
        assertThat(authorRepository.existsByName("存在しない著者")).isFalse()
    }

    @Test
    @DisplayName("全ての著者を取得できる")
    fun canRetrieveAllAuthors() {
        // Given
        authorRepository.insert(testAuthor)
        val author2 = testAuthor.copy(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        authorRepository.insert(author2)

        // When
        val result = authorRepository.findAll()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactlyInAnyOrder("夏目漱石", "芥川龍之介")
    }

    @Test
    @DisplayName("著者情報を更新できる")
    fun canUpdateAuthorInformation() {
        // Given
        authorRepository.insert(testAuthor)
        val updateAuthor = testAuthor.copy(
            birthDate = LocalDate.of(1867, 1, 5), // 生年月日を変更
            updatedBy = "updater"
        )

        // When
        val result = authorRepository.update(updateAuthor)

        // Then
        assertThat(result.birthDate).isEqualTo(LocalDate.of(1867, 1, 5))
        assertThat(result.updatedBy).isEqualTo("updater")
        
        // DBから再取得して確認
        val found = authorRepository.findByName("夏目漱石")
        assertThat(found!!.birthDate).isEqualTo(LocalDate.of(1867, 1, 5))
    }

    @Test
    @DisplayName("存在しない著者を更新しようとすると例外が発生する")
    fun throwsExceptionWhenUpdatingNonExistentAuthor() {
        // When & Then
        assertThatThrownBy {
            authorRepository.update(testAuthor)
        }.isInstanceOf(NoSuchElementException::class.java)
         .hasMessageContaining("指定された著者が見つかりません")
    }

    @Test
    @DisplayName("著者を削除できる")
    fun canDeleteAuthor() {
        // Given
        authorRepository.insert(testAuthor)
        assertThat(authorRepository.existsByName("夏目漱石")).isTrue()

        // When
        val deletedCount = authorRepository.deleteByName("夏目漱石")

        // Then
        assertThat(deletedCount).isEqualTo(1)
        assertThat(authorRepository.existsByName("夏目漱石")).isFalse()
    }

    @Test
    @DisplayName("存在しない著者を削除しようとすると0件が返される")
    fun returnsZeroWhenDeletingNonExistentAuthor() {
        // When
        val deletedCount = authorRepository.deleteByName("存在しない著者")

        // Then
        assertThat(deletedCount).isEqualTo(0)
    }
}