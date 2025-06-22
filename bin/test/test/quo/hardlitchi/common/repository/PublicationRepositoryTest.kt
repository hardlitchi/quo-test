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
import test.quo.hardlitchi.common.entity.*
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * PublicationRepositoryのテストクラス
 * TDD: Publication機能のRepository層をテスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicationRepositoryTest {

    @Autowired
    lateinit var publicationRepository: PublicationRepository

    @Autowired
    lateinit var authorRepository: AuthorRepository

    @Autowired
    lateinit var bookRepository: BookRepository
    
    @Autowired
    lateinit var dslContext: DSLContext

    private lateinit var testPublication: Publication
    private lateinit var testAuthor: Author
    private lateinit var testBook: Book

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
        
        val now = LocalDateTime.now()
        
        testAuthor = Author(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
            createdAt = now,
            createdBy = "test",
            updatedAt = now,
            updatedBy = "test"
        )

        testBook = Book(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            createdAt = now,
            createdBy = "test",
            updatedAt = now,
            updatedBy = "test"
        )

        testPublication = Publication(
            bookTitle = "こころ",
            authorName = "夏目漱石",
            createdAt = now,
            createdBy = "test",
            updatedAt = now,
            updatedBy = "test"
        )

        // 事前に著者と書籍を登録
        authorRepository.insert(testAuthor)
        bookRepository.insert(testBook)
    }

    @Test
    @DisplayName("出版関係を正常に登録できる")
    fun canInsertPublicationSuccessfully() {
        // When
        val result = publicationRepository.insert(testPublication)

        // Then
        assertThat(result.bookTitle).isEqualTo("こころ")
        assertThat(result.authorName).isEqualTo("夏目漱石")
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
        
        // 実際にDBに保存されたかを確認
        val found = publicationRepository.findById(PublicationId("こころ", "夏目漱石"))
        assertThat(found).isNotNull
        assertThat(found!!.bookTitle).isEqualTo("こころ")
        assertThat(found.authorName).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("重複する出版関係で登録しようとすると例外が発生する")
    fun throwsExceptionWhenInsertingDuplicatePublication() {
        // Given: 既に出版関係が登録済み
        publicationRepository.insert(testPublication)

        // When & Then: 同じ出版関係を再度登録しようとすると例外発生
        assertThatThrownBy {
            publicationRepository.insert(testPublication)
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    @DisplayName("IDで出版関係を検索できる")
    fun canFindPublicationById() {
        // Given
        publicationRepository.insert(testPublication)

        // When
        val result = publicationRepository.findById(PublicationId("こころ", "夏目漱石"))

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.bookTitle).isEqualTo("こころ")
        assertThat(result.authorName).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("存在しないIDで検索するとnullが返される")
    fun returnsNullWhenSearchingWithNonExistentId() {
        // When
        val result = publicationRepository.findById(PublicationId("存在しない書籍", "存在しない著者"))

        // Then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("書籍タイトルで出版関係を検索できる")
    fun canFindPublicationsByBookTitle() {
        // Given
        publicationRepository.insert(testPublication)
        
        // 別の著者も追加
        val author2 = testAuthor.copy(name = "芥川龍之介", birthDate = LocalDate.of(1892, 3, 1))
        authorRepository.insert(author2)
        val publication2 = testPublication.copy(authorName = "芥川龍之介")
        publicationRepository.insert(publication2)

        // When
        val publications = publicationRepository.findByBookTitle("こころ")

        // Then
        assertThat(publications).hasSize(2)
        assertThat(publications.map { it.authorName }).containsExactlyInAnyOrder("夏目漱石", "芥川龍之介")
    }

    @Test
    @DisplayName("著者名で出版関係を検索できる")
    fun canFindPublicationsByAuthorName() {
        // Given
        publicationRepository.insert(testPublication)
        
        // 別の書籍も追加
        val book2 = testBook.copy(title = "吾輩は猫である", price = BigDecimal("600"))
        bookRepository.insert(book2)
        val publication2 = testPublication.copy(bookTitle = "吾輩は猫である")
        publicationRepository.insert(publication2)

        // When
        val publications = publicationRepository.findByAuthorName("夏目漱石")

        // Then
        assertThat(publications).hasSize(2)
        assertThat(publications.map { it.bookTitle }).containsExactlyInAnyOrder("こころ", "吾輩は猫である")
    }

    @Test
    @DisplayName("出版関係を削除できる")
    fun canDeletePublication() {
        // Given
        publicationRepository.insert(testPublication)
        val id = PublicationId("こころ", "夏目漱石")
        assertThat(publicationRepository.existsById(id)).isTrue()

        // When
        val deletedCount = publicationRepository.deleteById(id)

        // Then
        assertThat(deletedCount).isEqualTo(1)
        assertThat(publicationRepository.existsById(id)).isFalse()
    }

    @Test
    @DisplayName("書籍タイトルで全ての出版関係を削除できる")
    fun canDeleteAllPublicationsByBookTitle() {
        // Given
        publicationRepository.insert(testPublication)
        
        val author2 = testAuthor.copy(name = "芥川龍之介", birthDate = LocalDate.of(1892, 3, 1))
        authorRepository.insert(author2)
        val publication2 = testPublication.copy(authorName = "芥川龍之介")
        publicationRepository.insert(publication2)

        // When
        val deletedCount = publicationRepository.deleteByBookTitle("こころ")

        // Then
        assertThat(deletedCount).isEqualTo(2)
        assertThat(publicationRepository.findByBookTitle("こころ")).isEmpty()
    }

    @Test
    @DisplayName("著者名で全ての出版関係を削除できる")
    fun canDeleteAllPublicationsByAuthorName() {
        // Given
        publicationRepository.insert(testPublication)
        
        val book2 = testBook.copy(title = "吾輩は猫である", price = BigDecimal("600"))
        bookRepository.insert(book2)
        val publication2 = testPublication.copy(bookTitle = "吾輩は猫である")
        publicationRepository.insert(publication2)

        // When
        val deletedCount = publicationRepository.deleteByAuthorName("夏目漱石")

        // Then
        assertThat(deletedCount).isEqualTo(2)
        assertThat(publicationRepository.findByAuthorName("夏目漱石")).isEmpty()
    }

    @Test
    @DisplayName("出版関係の存在チェックができる")
    fun canCheckIfPublicationExists() {
        // Given
        publicationRepository.insert(testPublication)

        // When & Then
        val id1 = PublicationId("こころ", "夏目漱石")
        val id2 = PublicationId("存在しない書籍", "存在しない著者")
        
        assertThat(publicationRepository.existsById(id1)).isTrue()
        assertThat(publicationRepository.existsById(id2)).isFalse()
    }
}