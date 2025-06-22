package test.quo.hardlitchi.common.repository

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * BookRepositoryの簡単なテスト
 * TDD: GREEN フェーズ確認用
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookRepositorySimpleTest {

    @Autowired
    lateinit var bookRepository: BookRepository
    
    @Autowired
    lateinit var dslContext: DSLContext

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
        testBook = Book(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            createdAt = now,
            createdBy = "test",
            updatedAt = now,
            updatedBy = "test"
        )
    }

    @Test
    fun testInsertBook() {
        // When
        val result = bookRepository.insert(testBook)

        // Then
        assertThat(result.title).isEqualTo("こころ")
        assertThat(result.price).isEqualTo(BigDecimal("500"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.UNPUBLISHED)
    }

    @Test
    fun testFindByTitle() {
        // Given
        bookRepository.insert(testBook)

        // When
        val result = bookRepository.findByTitle("こころ")

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.title).isEqualTo("こころ")
    }

    @Test
    fun testExistsByTitle() {
        // Given
        bookRepository.insert(testBook)

        // When & Then
        assertThat(bookRepository.existsByTitle("こころ")).isTrue()
        assertThat(bookRepository.existsByTitle("存在しない書籍")).isFalse()
    }
}