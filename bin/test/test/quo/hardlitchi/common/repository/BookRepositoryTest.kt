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
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * BookRepositoryのテストクラス
 * TDD: Book機能のRepository層をテスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookRepositoryTest {

    @Autowired
    lateinit var bookRepository: BookRepository

    @Autowired
    lateinit var authorRepository: AuthorRepository

    @Autowired
    lateinit var publicationRepository: PublicationRepository
    
    @Autowired
    lateinit var dslContext: DSLContext

    private lateinit var testBook: Book
    private lateinit var testAuthor: Author
    private lateinit var testPublication: Publication

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

        testAuthor = Author(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
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
    }

    @Test
    @DisplayName("書籍を正常に登録できる")
    fun canInsertBookSuccessfully() {
        // When
        val result = bookRepository.insert(testBook)

        // Then
        assertThat(result.title).isEqualTo("こころ")
        assertThat(result.price).isEqualByComparingTo(BigDecimal("500"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.UNPUBLISHED)
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
        
        // 実際にDBに保存されたかを確認
        val found = bookRepository.findByTitle("こころ")
        assertThat(found).isNotNull
        assertThat(found!!.title).isEqualTo("こころ")
    }

    @Test
    @DisplayName("重複するタイトルで登録しようとすると例外が発生する")
    fun throwsExceptionWhenInsertingDuplicateTitle() {
        // Given: 既に書籍が登録済み
        bookRepository.insert(testBook)

        // When & Then: 同じタイトルの書籍を再度登録しようとすると例外発生
        assertThatThrownBy {
            bookRepository.insert(testBook)
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    @DisplayName("存在する書籍をタイトルで検索できる")
    fun canFindExistingBookByTitle() {
        // Given
        bookRepository.insert(testBook)

        // When
        val result = bookRepository.findByTitle("こころ")

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.title).isEqualTo("こころ")
        assertThat(result.price).isEqualByComparingTo(BigDecimal("500"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.UNPUBLISHED)
    }

    @Test
    @DisplayName("存在しない書籍をタイトルで検索するとnullが返される")
    fun returnsNullWhenSearchingForNonExistentBook() {
        // When
        val result = bookRepository.findByTitle("存在しない書籍")

        // Then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("書籍が存在するかチェックできる")
    fun canCheckIfBookExists() {
        // Given
        bookRepository.insert(testBook)

        // When & Then
        assertThat(bookRepository.existsByTitle("こころ")).isTrue()
        assertThat(bookRepository.existsByTitle("存在しない書籍")).isFalse()
    }

    @Test
    @DisplayName("全ての書籍を取得できる")
    fun canRetrieveAllBooks() {
        // Given
        bookRepository.insert(testBook)
        val book2 = testBook.copy(
            title = "吾輩は猫である",
            price = BigDecimal("600")
        )
        bookRepository.insert(book2)

        // When
        val result = bookRepository.findAll()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.title }).containsExactlyInAnyOrder("こころ", "吾輩は猫である")
    }

    @Test
    @DisplayName("書籍情報を更新できる")
    fun canUpdateBookInformation() {
        // Given
        bookRepository.insert(testBook)
        val updateBook = testBook.copy(
            price = BigDecimal("700"),
            publicationStatus = PublicationStatus.PUBLISHED,
            updatedBy = "updater"
        )

        // When
        val result = bookRepository.update(updateBook)

        // Then
        assertThat(result.price).isEqualTo(BigDecimal("700"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
        assertThat(result.updatedBy).isEqualTo("updater")
        
        // DBから再取得して確認
        val found = bookRepository.findByTitle("こころ")
        assertThat(found!!.price).isEqualByComparingTo(BigDecimal("700"))
        assertThat(found.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
    }

    @Test
    @DisplayName("存在しない書籍を更新しようとすると例外が発生する")
    fun throwsExceptionWhenUpdatingNonExistentBook() {
        // When & Then
        assertThatThrownBy {
            bookRepository.update(testBook)
        }.isInstanceOf(NoSuchElementException::class.java)
         .hasMessageContaining("指定された書籍が見つかりません")
    }

    @Test
    @DisplayName("書籍を削除できる")
    fun canDeleteBook() {
        // Given
        bookRepository.insert(testBook)
        assertThat(bookRepository.existsByTitle("こころ")).isTrue()

        // When
        val deletedCount = bookRepository.deleteByTitle("こころ")

        // Then
        assertThat(deletedCount).isEqualTo(1)
        assertThat(bookRepository.existsByTitle("こころ")).isFalse()
    }

    @Test
    @DisplayName("存在しない書籍を削除しようとすると0件が返される")
    fun returnsZeroWhenDeletingNonExistentBook() {
        // When
        val deletedCount = bookRepository.deleteByTitle("存在しない書籍")

        // Then
        assertThat(deletedCount).isEqualTo(0)
    }

    @Test
    @DisplayName("出版状況で書籍を検索できる")
    fun canFindBooksByPublicationStatus() {
        // Given
        bookRepository.insert(testBook)
        val publishedBook = testBook.copy(
            title = "吾輩は猫である",
            publicationStatus = PublicationStatus.PUBLISHED
        )
        bookRepository.insert(publishedBook)

        // When
        val unpublishedBooks = bookRepository.findByPublicationStatus(PublicationStatus.UNPUBLISHED)
        val publishedBooks = bookRepository.findByPublicationStatus(PublicationStatus.PUBLISHED)

        // Then
        assertThat(unpublishedBooks).hasSize(1)
        assertThat(unpublishedBooks[0].title).isEqualTo("こころ")
        assertThat(publishedBooks).hasSize(1)
        assertThat(publishedBooks[0].title).isEqualTo("吾輩は猫である")
    }

    @Test
    @DisplayName("著者名で書籍を検索できる")
    fun canFindBooksByAuthorName() {
        // Given: 著者を登録
        authorRepository.insert(testAuthor)
        
        // 書籍を登録
        bookRepository.insert(testBook)
        
        // 出版関係を登録
        publicationRepository.insert(testPublication)

        // When
        val books = bookRepository.findByAuthorName("夏目漱石")

        // Then
        assertThat(books).hasSize(1)
        assertThat(books[0].title).isEqualTo("こころ")
    }

    @Test
    @DisplayName("存在しない著者名で書籍を検索すると空のリストが返される")
    fun returnsEmptyListWhenSearchingBooksWithNonExistentAuthor() {
        // When
        val books = bookRepository.findByAuthorName("存在しない著者")

        // Then
        assertThat(books).isEmpty()
    }
}