package test.quo.hardlitchi.web.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.common.service.BookService
import test.quo.hardlitchi.web.bean.CreateBookRequest
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * BookControllerの単純なユニットテスト
 * TDD: GREEN フェーズ確認用
 */
class BookControllerSimpleTest {

    @Mock
    lateinit var bookService: BookService

    lateinit var bookController: BookController

    private lateinit var sampleBook: Book
    private lateinit var createRequest: CreateBookRequest

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        bookController = BookController(bookService)

        sampleBook = Book(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            createdAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            createdBy = "system",
            updatedAt = LocalDateTime.of(2024, 1, 1, 10, 0),
            updatedBy = "system"
        )

        createRequest = CreateBookRequest(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf("夏目漱石")
        )
    }

    @Test
    fun testCreateBook() {
        // Given
        given(bookService.createBook(createRequest.toServiceDto()))
            .willReturn(sampleBook)
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When
        val response = bookController.createBook(createRequest)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data?.title).isEqualTo("こころ")
        assertThat(response.body?.data?.authors).containsExactly("夏目漱石")
        assertThat(response.body?.message).isEqualTo("書籍を登録しました")
    }

    @Test
    fun testGetBook() {
        // Given
        given(bookService.findByTitle("こころ"))
            .willReturn(sampleBook)
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When
        val response = bookController.getBook("こころ")

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data?.title).isEqualTo("こころ")
        assertThat(response.body?.data?.authors).containsExactly("夏目漱石")
    }

    @Test
    fun testGetBookNotFound() {
        // Given
        given(bookService.findByTitle("存在しない書籍"))
            .willReturn(null)

        // When
        val response = bookController.getBook("存在しない書籍")

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.success).isFalse()
        assertThat(response.body?.message).isEqualTo("書籍が見つかりません: 存在しない書籍")
    }

    @Test
    fun testGetBooks() {
        // Given
        given(bookService.findAll())
            .willReturn(listOf(sampleBook))
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When
        val response = bookController.getBooks(null, null)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.data).hasSize(1)
        assertThat(response.body?.data?.get(0)?.title).isEqualTo("こころ")
    }
}