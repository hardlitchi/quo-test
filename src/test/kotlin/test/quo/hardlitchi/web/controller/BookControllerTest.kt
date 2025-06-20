package test.quo.hardlitchi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.BDDMockito.*
import org.mockito.kotlin.any
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.common.service.*
import test.quo.hardlitchi.web.bean.CreateBookRequest
import test.quo.hardlitchi.web.bean.UpdateBookRequest
import test.quo.hardlitchi.web.exception.GlobalExceptionHandler
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * BookControllerのテストクラス
 * TDD: REST API層のテスト
 */
@WebMvcTest(controllers = [BookController::class, GlobalExceptionHandler::class])
class BookControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var bookService: BookService

    private lateinit var sampleBook: Book
    private lateinit var createRequest: CreateBookRequest
    private lateinit var updateRequest: UpdateBookRequest

    @BeforeEach
    fun setUp() {
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

        updateRequest = UpdateBookRequest(
            price = BigDecimal("600"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf("夏目漱石")
        )
    }

    @Test
    @DisplayName("正常な書籍登録リクエストで201が返される")
    fun returns201ForValidBookCreationRequest() {
        // Given
        given(bookService.createBook(any<CreateBookDto>()))
            .willReturn(sampleBook)
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title").value("こころ"))
        .andExpect(jsonPath("$.data.price").value(500))
        .andExpect(jsonPath("$.data.publicationStatus").value("UNPUBLISHED"))
        .andExpect(jsonPath("$.data.authors[0]").value("夏目漱石"))
        .andExpect(jsonPath("$.message").value("書籍を登録しました"))
    }

    @Test
    @DisplayName("空文字のタイトルで登録しようとすると400が返される")
    fun returns400ForEmptyTitle() {
        // Given
        val invalidRequest = createRequest.copy(title = "")

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("負の価格で登録しようとすると400が返される")
    fun returns400ForNegativePrice() {
        // Given
        val invalidRequest = createRequest.copy(price = BigDecimal("-100"))

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("著者が存在しない場合404が返される")
    fun returns404WhenAuthorDoesNotExist() {
        // Given
        given(bookService.createBook(any<CreateBookDto>()))
            .willThrow(ResourceNotFoundException("著者が見つかりません: 存在しない著者"))

        val invalidRequest = createRequest.copy(authors = listOf("存在しない著者"))

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("著者が見つかりません: 存在しない著者"))
    }

    @Test
    @DisplayName("重複するタイトルで登録しようとすると409が返される")
    fun returns409ForDuplicateTitle() {
        // Given
        given(bookService.createBook(any<CreateBookDto>()))
            .willThrow(DuplicateResourceException("書籍は既に存在します: こころ"))

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isConflict)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("書籍は既に存在します: こころ"))
    }

    @Test
    @DisplayName("存在する書籍情報の更新で200が返される")
    fun returns200ForUpdatingExistingBook() {
        // Given
        val updatedBook = sampleBook.copy(
            price = BigDecimal("600"),
            publicationStatus = PublicationStatus.PUBLISHED,
            updatedAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )
        given(bookService.updateBook(any<UpdateBookDto>()))
            .willReturn(updatedBook)
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When & Then
        mockMvc.perform(
            put("/api/books/こころ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.title").value("こころ"))
        .andExpect(jsonPath("$.data.price").value(600))
        .andExpect(jsonPath("$.data.publicationStatus").value("PUBLISHED"))
        .andExpect(jsonPath("$.message").value("書籍情報を更新しました"))
    }

    @Test
    @DisplayName("存在しない書籍を更新しようとすると404が返される")
    fun returns404ForUpdatingNonExistentBook() {
        // Given
        given(bookService.updateBook(any<UpdateBookDto>()))
            .willThrow(ResourceNotFoundException("書籍が見つかりません: 存在しない書籍"))

        // When & Then
        mockMvc.perform(
            put("/api/books/存在しない書籍")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("書籍が見つかりません: 存在しない書籍"))
    }

    @Test
    @DisplayName("出版済み書籍を未出版に戻そうとすると400が返される")
    fun returns400ForChangingPublishedBookToUnpublished() {
        // Given
        given(bookService.updateBook(any<UpdateBookDto>()))
            .willThrow(IllegalArgumentException("出版済み書籍を未出版に戻すことはできません"))

        val invalidRequest = updateRequest.copy(publicationStatus = PublicationStatus.UNPUBLISHED)

        // When & Then
        mockMvc.perform(
            put("/api/books/こころ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("出版済み書籍を未出版に戻すことはできません"))
    }

    @Test
    @DisplayName("存在する書籍の取得で200が返される")
    fun returns200ForRetrievingExistingBook() {
        // Given
        given(bookService.findByTitle("こころ"))
            .willReturn(sampleBook)
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When & Then
        mockMvc.perform(get("/api/books/こころ"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("こころ"))
            .andExpect(jsonPath("$.data.price").value(500))
            .andExpect(jsonPath("$.data.authors[0]").value("夏目漱石"))
            .andExpect(jsonPath("$.message").isEmpty)
    }

    @Test
    @DisplayName("存在しない書籍の取得で404が返される")
    fun returns404ForRetrievingNonExistentBook() {
        // Given
        given(bookService.findByTitle("存在しない書籍"))
            .willReturn(null)

        // When & Then
        mockMvc.perform(get("/api/books/存在しない書籍"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("書籍一覧取得で200が返される")
    fun returns200ForRetrievingBookList() {
        // Given
        val book2 = sampleBook.copy(
            title = "吾輩は猫である",
            price = BigDecimal("400")
        )
        given(bookService.findAll())
            .willReturn(listOf(sampleBook, book2))
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))
        given(bookService.getAuthorsForBook("吾輩は猫である"))
            .willReturn(listOf("夏目漱石"))

        // When & Then
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].title").value("こころ"))
            .andExpect(jsonPath("$.data[1].title").value("吾輩は猫である"))
    }

    @Test
    @DisplayName("出版状況でフィルタした書籍一覧取得で200が返される")
    fun returns200ForRetrievingBooksFilteredByPublicationStatus() {
        // Given
        given(bookService.findByPublicationStatus(PublicationStatus.UNPUBLISHED))
            .willReturn(listOf(sampleBook))
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When & Then
        mockMvc.perform(get("/api/books?status=UNPUBLISHED"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].publicationStatus").value("UNPUBLISHED"))
    }

    @Test
    @DisplayName("著者名でフィルタした書籍一覧取得で200が返される")
    fun returns200ForRetrievingBooksFilteredByAuthorName() {
        // Given
        given(bookService.findByAuthorName("夏目漱石"))
            .willReturn(listOf(sampleBook))
        given(bookService.getAuthorsForBook("こころ"))
            .willReturn(listOf("夏目漱石"))

        // When & Then
        mockMvc.perform(get("/api/books?author=夏目漱石"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].authors[0]").value("夏目漱石"))
    }

    @Test
    @DisplayName("JSONパース失敗で400が返される")
    fun returns400ForJsonParseFailure() {
        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("Content-Typeが不正な場合415が返される")
    fun returns415ForInvalidContentType() {
        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @DisplayName("必須フィールドが欠けているリクエストで400が返される")
    fun returns400ForRequestWithMissingRequiredFields() {
        // Given
        val incompleteJson = """{"title": "こころ"}"""

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteJson)
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("サービス層で予期しない例外が発生した場合500が返される")
    fun returns500ForUnexpectedExceptionInServiceLayer() {
        // Given
        given(bookService.createBook(any<CreateBookDto>()))
            .willThrow(RuntimeException("データベース接続エラー"))

        // When & Then
        mockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isInternalServerError)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }
}