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
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.common.service.*
import test.quo.hardlitchi.web.bean.CreateAuthorRequest
import test.quo.hardlitchi.web.bean.UpdateAuthorRequest
import test.quo.hardlitchi.web.exception.GlobalExceptionHandler
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AuthorControllerのテストクラス
 * TDD: REST API層のテスト
 */
@WebMvcTest(controllers = [AuthorController::class, GlobalExceptionHandler::class])
class AuthorControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var authorService: AuthorService
    
    @MockBean
    lateinit var bookService: BookService

    private lateinit var sampleAuthor: Author
    private lateinit var createRequest: CreateAuthorRequest
    private lateinit var updateRequest: UpdateAuthorRequest

    @BeforeEach
    fun setUp() {
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

        updateRequest = UpdateAuthorRequest(
            birthDate = LocalDate.of(1867, 1, 5)
        )
    }

    @Test
    @DisplayName("正常な著者登録リクエストで201が返される")
    fun returns201ForValidAuthorCreationRequest() {
        // Given
        given(authorService.createAuthor(any<CreateAuthorDto>()))
            .willReturn(sampleAuthor)

        // When & Then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("夏目漱石"))
        .andExpect(jsonPath("$.data.birthDate").value("1867-02-09"))
        .andExpect(jsonPath("$.message").value("著者を登録しました"))
    }

    @Test
    @DisplayName("空文字の著者名で登録しようとすると400が返される")
    fun returns400ForEmptyAuthorName() {
        // Given
        val invalidRequest = createRequest.copy(name = "")

        // When & Then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("未来の生年月日で登録しようとすると400が返される")
    fun returns400ForFutureBirthDate() {
        // Given
        val invalidRequest = createRequest.copy(birthDate = LocalDate.now().plusDays(1))

        // When & Then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("重複する著者名で登録しようとすると409が返される")
    fun returns409ForDuplicateAuthorName() {
        // Given
        given(authorService.createAuthor(any<CreateAuthorDto>()))
            .willThrow(DuplicateResourceException("著者は既に存在します: 夏目漱石"))

        // When & Then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isConflict)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("著者は既に存在します: 夏目漱石"))
    }

    @Test
    @DisplayName("存在する著者情報の更新で200が返される")
    fun returns200ForUpdatingExistingAuthor() {
        // Given
        val updatedAuthor = sampleAuthor.copy(
            birthDate = LocalDate.of(1867, 1, 5),
            updatedAt = LocalDateTime.of(2024, 1, 1, 12, 0)
        )
        given(authorService.updateAuthor(any<UpdateAuthorDto>()))
            .willReturn(updatedAuthor)

        // When & Then
        mockMvc.perform(
            put("/api/authors/夏目漱石")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("夏目漱石"))
        .andExpect(jsonPath("$.data.birthDate").value("1867-01-05"))
        .andExpect(jsonPath("$.message").value("著者情報を更新しました"))
    }

    @Test
    @DisplayName("存在しない著者を更新しようとすると404が返される")
    fun returns404ForUpdatingNonExistentAuthor() {
        // Given
        given(authorService.updateAuthor(any<UpdateAuthorDto>()))
            .willThrow(ResourceNotFoundException("著者が見つかりません: 存在しない著者"))

        // When & Then
        mockMvc.perform(
            put("/api/authors/存在しない著者")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("著者が見つかりません: 存在しない著者"))
    }

    @Test
    @DisplayName("存在する著者の取得で200が返される")
    fun returns200ForRetrievingExistingAuthor() {
        // Given
        given(authorService.findByName("夏目漱石"))
            .willReturn(sampleAuthor)

        // When & Then
        mockMvc.perform(get("/api/authors/夏目漱石"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("夏目漱石"))
            .andExpect(jsonPath("$.data.birthDate").value("1867-02-09"))
    }

    @Test
    @DisplayName("存在しない著者の取得で404が返される")
    fun returns404ForRetrievingNonExistentAuthor() {
        // Given
        given(authorService.findByName("存在しない著者"))
            .willReturn(null)

        // When & Then
        mockMvc.perform(get("/api/authors/存在しない著者"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists())
    }

    @Test
    @DisplayName("著者一覧取得で200が返される")
    fun returns200ForRetrievingAuthorList() {
        // Given
        val author2 = sampleAuthor.copy(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        given(authorService.findAll())
            .willReturn(listOf(sampleAuthor, author2))

        // When & Then
        mockMvc.perform(get("/api/authors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].name").value("夏目漱石"))
            .andExpect(jsonPath("$.data[1].name").value("芥川龍之介"))
    }

    @Test
    @DisplayName("空の著者一覧取得で200が返される")
    fun returns200ForRetrievingEmptyAuthorList() {
        // Given
        given(authorService.findAll())
            .willReturn(emptyList())

        // When & Then
        mockMvc.perform(get("/api/authors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(0))
    }

    @Test
    @DisplayName("JSONパース失敗で400が返される")
    fun returns400ForJsonParseFailure() {
        // When & Then
        mockMvc.perform(
            post("/api/authors")
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
            post("/api/authors")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isUnsupportedMediaType)
    }

    @Test
    @DisplayName("必須フィールドが欠けているリクエストで400が返される")
    fun returns400ForRequestWithMissingRequiredFields() {
        // Given
        val incompleteJson = """{"name": "夏目漱石"}"""

        // When & Then
        mockMvc.perform(
            post("/api/authors")
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
        given(authorService.createAuthor(any<CreateAuthorDto>()))
            .willThrow(RuntimeException("データベース接続エラー"))

        // When & Then
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isInternalServerError)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").exists())
    }
}