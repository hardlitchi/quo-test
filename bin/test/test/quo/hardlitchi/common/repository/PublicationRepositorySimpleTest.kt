package test.quo.hardlitchi.common.repository

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
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
 * PublicationRepositoryの簡単なテスト
 * TDD: GREEN フェーズ確認用
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicationRepositorySimpleTest {

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
    fun testInsertPublication() {
        // When
        val result = publicationRepository.insert(testPublication)

        // Then
        assertThat(result.bookTitle).isEqualTo("こころ")
        assertThat(result.authorName).isEqualTo("夏目漱石")
    }

    @Test
    fun testFindById() {
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
    fun testExistsById() {
        // Given
        publicationRepository.insert(testPublication)

        // When & Then
        val id1 = PublicationId("こころ", "夏目漱石")
        val id2 = PublicationId("存在しない書籍", "存在しない著者")
        
        assertThat(publicationRepository.existsById(id1)).isTrue()
        assertThat(publicationRepository.existsById(id2)).isFalse()
    }
}