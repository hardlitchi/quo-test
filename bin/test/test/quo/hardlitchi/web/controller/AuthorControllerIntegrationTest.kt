package test.quo.hardlitchi.web.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.web.bean.CreateAuthorRequest
import java.time.LocalDate
import java.util.*

/**
 * AuthorControllerの統合テスト
 * TDD: Controller層の統合テスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class AuthorControllerIntegrationTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @LocalServerPort
    var port: Int = 0

    @Test
    @DisplayName("正常な著者登録リクエストで201が返される")
    fun returns201ForValidAuthorCreationRequest() {
        val uniqueName = "夏目漱石_${UUID.randomUUID()}"
        val createRequest = CreateAuthorRequest(
            name = uniqueName,
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(createRequest, headers)
        
        val response = testRestTemplate.postForEntity(
            "/api/authors",
            entity,
            String::class.java
        )
        
        assert(response.statusCode == HttpStatus.CREATED)
        assert(response.body!!.contains(uniqueName))
    }

    @Test
    @DisplayName("重複する著者名で登録しようとすると409が返される")
    fun returns409ForDuplicateAuthorName() {
        val uniqueName = "夏目漱石_${UUID.randomUUID()}"
        val createRequest = CreateAuthorRequest(
            name = uniqueName,
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(createRequest, headers)
        
        // 最初の著者を登録
        testRestTemplate.postForEntity(
            "/api/authors",
            entity,
            String::class.java
        )
        
        // 同じ名前の著者を再度登録しようとする
        val response = testRestTemplate.postForEntity(
            "/api/authors",
            entity,
            String::class.java
        )
        
        assert(response.statusCode == HttpStatus.CONFLICT)
        assert(response.body!!.contains("著者は既に存在します: $uniqueName"))
    }

    @Test
    @DisplayName("存在する著者の取得で200が返される")
    fun returns200ForRetrievingExistingAuthor() {
        val uniqueName = "夏目漱石_${UUID.randomUUID()}"
        val createRequest = CreateAuthorRequest(
            name = uniqueName,
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(createRequest, headers)
        
        // 著者を登録
        testRestTemplate.postForEntity(
            "/api/authors",
            entity,
            String::class.java
        )
        
        // 著者を取得
        val response = testRestTemplate.getForEntity(
            "/api/authors/$uniqueName",
            String::class.java
        )
        
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body!!.contains(uniqueName))
    }

    @Test
    @DisplayName("存在しない著者の取得で404が返される")
    fun returns404ForRetrievingNonExistentAuthor() {
        val nonExistentName = "存在しない著者_${UUID.randomUUID()}"
        val response = testRestTemplate.getForEntity(
            "/api/authors/$nonExistentName",
            String::class.java
        )
        
        assert(response.statusCode == HttpStatus.NOT_FOUND)
        assert(response.body!!.contains("著者が見つかりません: $nonExistentName"))
    }

    @Test
    @DisplayName("著者一覧取得で200が返される")
    fun returns200ForRetrievingAuthorList() {
        val uniqueName = "夏目漱石_${UUID.randomUUID()}"
        val createRequest = CreateAuthorRequest(
            name = uniqueName,
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(createRequest, headers)
        
        // 著者を登録
        testRestTemplate.postForEntity(
            "/api/authors",
            entity,
            String::class.java
        )
        
        // 著者一覧を取得
        val response = testRestTemplate.getForEntity(
            "/api/authors",
            String::class.java
        )
        
        assert(response.statusCode == HttpStatus.OK)
        assert(response.body!!.contains(uniqueName))
    }
}