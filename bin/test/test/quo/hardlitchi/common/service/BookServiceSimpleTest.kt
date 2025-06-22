package test.quo.hardlitchi.common.service

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.math.BigDecimal
import java.time.LocalDate

/**
 * BookServiceの簡単なテスト
 * TDD: GREEN フェーズ確認用
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceSimpleTest {

    @Autowired
    lateinit var bookService: BookService
    
    @Autowired
    lateinit var authorService: AuthorService
    
    @Autowired
    lateinit var dslContext: DSLContext

    private lateinit var validBookDto: CreateBookDto

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
        
        // 著者を事前に登録
        val authorDto = CreateAuthorDto(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        authorService.createAuthor(authorDto)
        
        validBookDto = CreateBookDto(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf("夏目漱石")
        )
    }

    @Test
    fun testCreateBook() {
        // When
        val result = bookService.createBook(validBookDto)

        // Then
        assertThat(result.title).isEqualTo("こころ")
        assertThat(result.price).isEqualTo(BigDecimal("500"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.UNPUBLISHED)
    }

    @Test
    fun testFindByTitle() {
        // Given
        bookService.createBook(validBookDto)

        // When
        val result = bookService.findByTitle("こころ")

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.title).isEqualTo("こころ")
    }

    @Test
    fun testExistsByTitle() {
        // Given
        bookService.createBook(validBookDto)

        // When & Then
        assertThat(bookService.existsByTitle("こころ")).isTrue()
        assertThat(bookService.existsByTitle("存在しない書籍")).isFalse()
    }

    @Test
    fun testGetAuthorsForBook() {
        // Given
        bookService.createBook(validBookDto)

        // When
        val authors = bookService.getAuthorsForBook("こころ")

        // Then
        assertThat(authors).hasSize(1)
        assertThat(authors[0]).isEqualTo("夏目漱石")
    }
}