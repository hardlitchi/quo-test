package test.quo.hardlitchi.web.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.common.service.*
import test.quo.hardlitchi.web.bean.*

/**
 * 著者コントローラー
 * REST API エンドポイントを提供
 */
@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
    private val bookService: BookService
) {

    /**
     * 著者を登録する
     * POST /api/authors
     */
    @PostMapping
    fun createAuthor(@Valid @RequestBody request: CreateAuthorRequest): ResponseEntity<ApiResponse<AuthorResponse>> {
        val createdAuthor = authorService.createAuthor(request.toServiceDto())
        val response = ApiResponse(
            success = true,
            data = createdAuthor.toResponse(),
            message = "著者を登録しました"
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 著者情報を更新する
     * PUT /api/authors/{name}
     */
    @PutMapping("/{name}")
    fun updateAuthor(
        @PathVariable name: String,
        @Valid @RequestBody request: UpdateAuthorRequest
    ): ResponseEntity<ApiResponse<AuthorResponse>> {
        val updatedAuthor = authorService.updateAuthor(request.toServiceDto(name))
        val response = ApiResponse(
            success = true,
            data = updatedAuthor.toResponse(),
            message = "著者情報を更新しました"
        )
        return ResponseEntity.ok(response)
    }

    /**
     * 著者情報を取得する
     * GET /api/authors/{name}
     */
    @GetMapping("/{name}")
    fun getAuthor(@PathVariable name: String): ResponseEntity<ApiResponse<AuthorResponse>> {
        val author = authorService.findByName(name)
        return if (author != null) {
            val response = ApiResponse(
                success = true,
                data = author.toResponse(),
                message = null
            )
            ResponseEntity.ok(response)
        } else {
            val response = ApiResponse<AuthorResponse>(
                success = false,
                data = null,
                message = "著者が見つかりません: ${name}"
            )
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
        }
    }

    /**
     * 著者一覧を取得する
     * GET /api/authors
     */
    @GetMapping
    fun getAuthors(): ResponseEntity<ApiResponse<List<AuthorResponse>>> {
        val authors = authorService.findAll()
        val response = ApiResponse(
            success = true,
            data = authors.map { it.toResponse() },
            message = null
        )
        return ResponseEntity.ok(response)
    }

    /**
     * 著者出版書籍一覧を取得する
     * GET /api/authors/{name}/books
     */
    @GetMapping("/{name}/books")
    fun getAuthorBooks(@PathVariable name: String): ResponseEntity<ApiResponse<List<BookResponse>>> {
        // 著者の存在確認
        if (!authorService.existsByName(name)) {
            val response = ApiResponse<List<BookResponse>>(
                success = false,
                data = null,
                message = "著者が見つかりません: ${name}"
            )
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
        }

        // 著者の書籍を取得
        val books = bookService.findByAuthorName(name)
        val bookResponses = books.map { book ->
            val authors = bookService.getAuthorsForBook(book.title)
            BookResponse.from(book, authors)
        }

        val response = ApiResponse(
            success = true,
            data = bookResponses,
            message = null
        )
        return ResponseEntity.ok(response)
    }

    /**
     * AuthorエンティティをレスポンスDTOに変換
     */
    private fun Author.toResponse(): AuthorResponse {
        return AuthorResponse(
            name = name,
            birthDate = birthDate,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}