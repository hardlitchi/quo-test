package test.quo.hardlitchi.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.web.bean.CreateAuthorRequest
import test.quo.hardlitchi.web.bean.UpdateAuthorRequest
import java.time.LocalDate

/**
 * AuthorControllerの統合テスト
 * TDD: Controller層の統合テスト
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AuthorControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private lateinit var createRequest: CreateAuthorRequest
    private lateinit var updateRequest: UpdateAuthorRequest

    @BeforeEach
    fun setUp() {
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
    @DisplayName("重複する著者名で登録しようとすると409が返される")
    fun returns409ForDuplicateAuthorName() {
        // 最初の著者を登録
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)

        // 同じ名前の著者を再度登録しようとする
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
    @DisplayName("存在する著者の取得で200が返される")
    fun returns200ForRetrievingExistingAuthor() {
        // 著者を登録
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)

        // 著者を取得
        mockMvc.perform(get("/api/authors/夏目漱石"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("夏目漱石"))
            .andExpect(jsonPath("$.data.birthDate").value("1867-02-09"))
    }

    @Test
    @DisplayName("存在しない著者の取得で404が返される")
    fun returns404ForRetrievingNonExistentAuthor() {
        mockMvc.perform(get("/api/authors/存在しない著者"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("著者が見つかりません: 存在しない著者"))
    }

    @Test
    @DisplayName("著者一覧取得で200が返される")
    fun returns200ForRetrievingAuthorList() {
        // 著者を登録
        mockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
        .andExpect(status().isCreated)

        // 著者一覧を取得
        mockMvc.perform(get("/api/authors"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].name").value("夏目漱石"))
    }
}