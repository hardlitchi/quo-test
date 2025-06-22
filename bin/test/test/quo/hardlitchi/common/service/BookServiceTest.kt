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
import test.quo.hardlitchi.common.entity.*
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * BookServiceのテストクラス
 * TDD: Service層のビジネスロジックとトランザクション処理をテスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookServiceTest {

    @Autowired
    lateinit var bookService: BookService
    
    @Autowired
    lateinit var authorService: AuthorService
    
    @Autowired
    lateinit var dslContext: DSLContext

    private lateinit var validBookDto: CreateBookDto
    private lateinit var validAuthor: Author

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
        validAuthor = authorService.createAuthor(authorDto)
        
        validBookDto = CreateBookDto(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf("夏目漱石")
        )
    }

    @Test
    @DisplayName("正常な書籍を登録できる")
    fun canCreateValidBook() {
        // When
        val result = bookService.createBook(validBookDto)

        // Then
        assertThat(result.title).isEqualTo("こころ")
        assertThat(result.price).isEqualByComparingTo(BigDecimal("500"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.UNPUBLISHED)
        assertThat(result.createdAt).isNotNull()
        assertThat(result.updatedAt).isNotNull()
        
        // DBに実際に保存されていることを確認
        val found = bookService.findByTitle("こころ")
        assertThat(found).isNotNull
        assertThat(found!!.title).isEqualTo("こころ")
        
        // 出版関係も登録されていることを確認
        val publications = bookService.getAuthorsForBook("こころ")
        assertThat(publications).hasSize(1)
        assertThat(publications[0]).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("空文字のタイトルで登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingBookWithEmptyTitle() {
        // Given
        val invalidDto = validBookDto.copy(title = "")

        // When & Then
        assertThatThrownBy {
            bookService.createBook(invalidDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("書籍タイトルは必須です")
    }

    @Test
    @DisplayName("負の価格で登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingBookWithNegativePrice() {
        // Given
        val invalidDto = validBookDto.copy(price = BigDecimal("-100"))

        // When & Then
        assertThatThrownBy {
            bookService.createBook(invalidDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("価格は0以上である必要があります")
    }

    @Test
    @DisplayName("著者が存在しない場合例外が発生する")
    fun throwsExceptionWhenAuthorDoesNotExist() {
        // Given
        val invalidDto = validBookDto.copy(authors = listOf("存在しない著者"))

        // When & Then
        assertThatThrownBy {
            bookService.createBook(invalidDto)
        }.isInstanceOf(ResourceNotFoundException::class.java)
         .hasMessageContaining("著者が見つかりません: 存在しない著者")
    }

    @Test
    @DisplayName("著者が空のリストの場合例外が発生する")
    fun throwsExceptionWhenAuthorListIsEmpty() {
        // Given
        val invalidDto = validBookDto.copy(authors = emptyList())

        // When & Then
        assertThatThrownBy {
            bookService.createBook(invalidDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("書籍には最低1人の著者が必要です")
    }

    @Test
    @DisplayName("重複するタイトルで登録しようとすると例外が発生する")
    fun throwsExceptionWhenCreatingBookWithDuplicateTitle() {
        // Given: 既に書籍が登録済み
        bookService.createBook(validBookDto)

        // When & Then: 同じタイトルの書籍を再度登録しようとすると例外発生
        assertThatThrownBy {
            bookService.createBook(validBookDto)
        }.isInstanceOf(DuplicateResourceException::class.java)
         .hasMessageContaining("書籍は既に存在します: こころ")
    }

    @Test
    @DisplayName("複数の著者で書籍を登録できる")
    fun canCreateBookWithMultipleAuthors() {
        // Given: 別の著者も登録
        val author2Dto = CreateAuthorDto(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        authorService.createAuthor(author2Dto)
        
        val multiAuthorDto = validBookDto.copy(
            authors = listOf("夏目漱石", "芥川龍之介")
        )

        // When
        val result = bookService.createBook(multiAuthorDto)

        // Then
        assertThat(result.title).isEqualTo("こころ")
        
        // 複数の出版関係が登録されていることを確認
        val publications = bookService.getAuthorsForBook("こころ")
        assertThat(publications).hasSize(2)
        assertThat(publications).containsExactlyInAnyOrder("夏目漱石", "芥川龍之介")
    }

    @Test
    @DisplayName("存在する書籍を更新できる")
    fun canUpdateExistingBook() {
        // Given: 書籍を登録
        bookService.createBook(validBookDto)
        val updateDto = UpdateBookDto(
            title = "こころ",
            price = BigDecimal("600"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf("夏目漱石")
        )

        // When
        val result = bookService.updateBook(updateDto)

        // Then
        assertThat(result.price).isEqualByComparingTo(BigDecimal("600"))
        assertThat(result.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
        assertThat(result.title).isEqualTo("こころ")
        
        // DBで確認
        val found = bookService.findByTitle("こころ")
        assertThat(found!!.price).isEqualByComparingTo(BigDecimal("600"))
        assertThat(found.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
    }

    @Test
    @DisplayName("出版済み書籍を未出版に戻そうとすると例外が発生する")
    fun throwsExceptionWhenChangingPublishedBookToUnpublished() {
        // Given: 出版済み書籍を登録
        val publishedDto = validBookDto.copy(publicationStatus = PublicationStatus.PUBLISHED)
        bookService.createBook(publishedDto)
        
        val updateDto = UpdateBookDto(
            title = "こころ",
            price = BigDecimal("600"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf("夏目漱石")
        )

        // When & Then
        assertThatThrownBy {
            bookService.updateBook(updateDto)
        }.isInstanceOf(IllegalArgumentException::class.java)
         .hasMessageContaining("出版済み書籍を未出版に戻すことはできません")
    }

    @Test
    @DisplayName("存在しない書籍を更新しようとすると例外が発生する")
    fun throwsExceptionWhenUpdatingNonExistentBook() {
        // Given
        val updateDto = UpdateBookDto(
            title = "存在しない書籍",
            price = BigDecimal("600"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf("夏目漱石")
        )

        // When & Then
        assertThatThrownBy {
            bookService.updateBook(updateDto)
        }.isInstanceOf(ResourceNotFoundException::class.java)
         .hasMessageContaining("書籍が見つかりません: 存在しない書籍")
    }

    @Test
    @DisplayName("存在する書籍をタイトルで検索できる")
    fun canFindExistingBookByTitle() {
        // Given
        bookService.createBook(validBookDto)

        // When
        val result = bookService.findByTitle("こころ")

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.title).isEqualTo("こころ")
        assertThat(result.price).isEqualByComparingTo(BigDecimal("500"))
    }

    @Test
    @DisplayName("存在しない書籍をタイトルで検索するとnullが返される")
    fun returnsNullWhenSearchingForNonExistentBook() {
        // When
        val result = bookService.findByTitle("存在しない書籍")

        // Then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("全ての書籍を取得できる")
    fun canRetrieveAllBooks() {
        // Given
        bookService.createBook(validBookDto)
        
        // 別の書籍も登録
        val book2Dto = validBookDto.copy(title = "吾輩は猫である", price = BigDecimal("400"))
        bookService.createBook(book2Dto)

        // When
        val result = bookService.findAll()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.title }).containsExactlyInAnyOrder("こころ", "吾輩は猫である")
    }

    @Test
    @DisplayName("出版状況で書籍を検索できる")
    fun canFindBooksByPublicationStatus() {
        // Given
        bookService.createBook(validBookDto)
        val publishedDto = validBookDto.copy(
            title = "吾輩は猫である",
            publicationStatus = PublicationStatus.PUBLISHED
        )
        bookService.createBook(publishedDto)

        // When
        val unpublishedBooks = bookService.findByPublicationStatus(PublicationStatus.UNPUBLISHED)
        val publishedBooks = bookService.findByPublicationStatus(PublicationStatus.PUBLISHED)

        // Then
        assertThat(unpublishedBooks).hasSize(1)
        assertThat(unpublishedBooks[0].title).isEqualTo("こころ")
        assertThat(publishedBooks).hasSize(1)
        assertThat(publishedBooks[0].title).isEqualTo("吾輩は猫である")
    }

    @Test
    @DisplayName("著者名で書籍を検索できる")
    fun canFindBooksByAuthorName() {
        // Given
        bookService.createBook(validBookDto)
        val book2Dto = validBookDto.copy(title = "吾輩は猫である")
        bookService.createBook(book2Dto)

        // When
        val books = bookService.findByAuthorName("夏目漱石")

        // Then
        assertThat(books).hasSize(2)
        assertThat(books.map { it.title }).containsExactlyInAnyOrder("こころ", "吾輩は猫である")
    }

    @Test
    @DisplayName("書籍の著者一覧を取得できる")
    fun canGetAuthorsForBook() {
        // Given: 複数著者の書籍を登録
        val author2Dto = CreateAuthorDto(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        authorService.createAuthor(author2Dto)
        
        val multiAuthorDto = validBookDto.copy(
            authors = listOf("夏目漱石", "芥川龍之介")
        )
        bookService.createBook(multiAuthorDto)

        // When
        val authors = bookService.getAuthorsForBook("こころ")

        // Then
        assertThat(authors).hasSize(2)
        assertThat(authors).containsExactlyInAnyOrder("夏目漱石", "芥川龍之介")
    }

    @Test
    @DisplayName("書籍を削除できる")
    fun canDeleteBook() {
        // Given
        bookService.createBook(validBookDto)
        assertThat(bookService.existsByTitle("こころ")).isTrue()

        // When
        bookService.deleteByTitle("こころ")

        // Then
        assertThat(bookService.existsByTitle("こころ")).isFalse()
        assertThat(bookService.findByTitle("こころ")).isNull()
        
        // 出版関係も削除されていることを確認
        assertThat(bookService.getAuthorsForBook("こころ")).isEmpty()
    }

    @Test
    @DisplayName("存在しない書籍を削除しようとすると例外が発生する")
    fun throwsExceptionWhenDeletingNonExistentBook() {
        // When & Then
        assertThatThrownBy {
            bookService.deleteByTitle("存在しない書籍")
        }.isInstanceOf(ResourceNotFoundException::class.java)
         .hasMessageContaining("書籍が見つかりません: 存在しない書籍")
    }

    @Test
    @DisplayName("書籍の存在チェックができる")
    fun canCheckIfBookExists() {
        // Given
        bookService.createBook(validBookDto)

        // When & Then
        assertThat(bookService.existsByTitle("こころ")).isTrue()
        assertThat(bookService.existsByTitle("存在しない書籍")).isFalse()
    }

    @Test
    @DisplayName("トランザクションが正常にロールバックされる")
    fun transactionRollsBackProperly() {
        // Given
        bookService.createBook(validBookDto)
        
        // When & Then: 例外が発生した場合、既存データは変更されない
        assertThatThrownBy {
            bookService.createBook(validBookDto) // 重複エラー
        }.isInstanceOf(DuplicateResourceException::class.java)
        
        // 元のデータは変更されていないことを確認
        val found = bookService.findByTitle("こころ")
        assertThat(found).isNotNull
        assertThat(found!!.price).isEqualByComparingTo(BigDecimal("500"))
    }
}