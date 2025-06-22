package test.quo.hardlitchi.web.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.common.service.*
import test.quo.hardlitchi.web.bean.CreateAuthorRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AuthorControllerの単純なユニットテスト
 * TDD: GREEN フェーズ確認用
 */
class AuthorControllerSimpleTest {

    @Mock
    lateinit var authorService: AuthorService
    
    @Mock
    lateinit var bookService: BookService

    lateinit var authorController: AuthorController

    private lateinit var sampleAuthor: Author
    private lateinit var createRequest: CreateAuthorRequest

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authorController = AuthorController(authorService, bookService)

        sampleAuthor = Author(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
            createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            createdBy = "system",
            updatedAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            updatedBy = "system"
        )

        createRequest = CreateAuthorRequest(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
    }

    @Test
    @DisplayName("createAuthor()が正常に動作する")
    fun createAuthorWorksCorrectly() {
        // Given
        given(authorService.createAuthor(createRequest.toServiceDto()))
            .willReturn(sampleAuthor)

        // When
        val response = authorController.createAuthor(createRequest)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data?.name).isEqualTo("夏目漱石")
        assertThat(response.body?.message).isEqualTo("著者を登録しました")
    }

    @Test
    @DisplayName("getAuthor()で存在する著者を取得できる")
    fun canGetExistingAuthor() {
        // Given
        given(authorService.findByName("夏目漱石"))
            .willReturn(sampleAuthor)

        // When
        val response = authorController.getAuthor("夏目漱石")

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data?.name).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("getAuthor()で存在しない著者は404を返す")
    fun returns404ForNonExistentAuthor() {
        // Given
        given(authorService.findByName("存在しない著者"))
            .willReturn(null)

        // When
        val response = authorController.getAuthor("存在しない著者")

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.success).isFalse()
        assertThat(response.body?.message).isEqualTo("著者が見つかりません: 存在しない著者")
    }

    @Test
    @DisplayName("getAuthors()で著者一覧を取得できる")
    fun canGetAuthorsList() {
        // Given
        given(authorService.findAll())
            .willReturn(listOf(sampleAuthor))

        // When
        val response = authorController.getAuthors()

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data).hasSize(1)
        assertThat(response.body?.data?.get(0)?.name).isEqualTo("夏目漱石")
    }

    @Test
    @DisplayName("getAuthorBooks()で著者の書籍一覧を取得できる")
    fun canGetAuthorBooks() {
        // Given
        val book = Book(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.PUBLISHED,
            createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            createdBy = "system",
            updatedAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            updatedBy = "system"
        )
        
        given(authorService.existsByName("夏目漱石")).willReturn(true)
        given(bookService.findByAuthorName("夏目漱石")).willReturn(listOf(book))
        given(bookService.getAuthorsForBook("こころ")).willReturn(listOf("夏目漱石"))

        // When
        val response = authorController.getAuthorBooks("夏目漱石")

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data).hasSize(1)
        assertThat(response.body?.data?.get(0)?.title).isEqualTo("こころ")
        assertThat(response.body?.data?.get(0)?.authors).containsExactly("夏目漱石")
    }

    @Test
    @DisplayName("getAuthorBooks()で存在しない著者は404を返す")
    fun returns404ForNonExistentAuthorBooks() {
        // Given
        given(authorService.existsByName("存在しない著者")).willReturn(false)

        // When
        val response = authorController.getAuthorBooks("存在しない著者")

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.success).isFalse()
        assertThat(response.body?.message).isEqualTo("著者が見つかりません: 存在しない著者")
    }
}