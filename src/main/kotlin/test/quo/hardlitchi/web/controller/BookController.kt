package test.quo.hardlitchi.web.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.common.service.BookService
import test.quo.hardlitchi.web.bean.*

/**
 * 書籍コントローラー
 * REST API エンドポイントを提供
 */
@RestController
@RequestMapping("/api/books")
class BookController(private val bookService: BookService) {

    /**
     * 書籍を登録する
     * POST /api/books
     */
    @PostMapping
    fun createBook(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<ApiResponse<BookResponse>> {
        val createdBook = bookService.createBook(request.toServiceDto())
        val authors = bookService.getAuthorsForBook(createdBook.title)
        val response = ApiResponse(
            success = true,
            data = BookResponse.from(createdBook, authors),
            message = "書籍を登録しました"
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * 書籍情報を更新する
     * PUT /api/books/{title}
     */
    @PutMapping("/{title}")
    fun updateBook(
        @PathVariable title: String,
        @Valid @RequestBody request: UpdateBookRequest
    ): ResponseEntity<ApiResponse<BookResponse>> {
        val updatedBook = bookService.updateBook(request.toServiceDto(title))
        val authors = bookService.getAuthorsForBook(updatedBook.title)
        val response = ApiResponse(
            success = true,
            data = BookResponse.from(updatedBook, authors),
            message = "書籍情報を更新しました"
        )
        return ResponseEntity.ok(response)
    }

    /**
     * 書籍情報を取得する
     * GET /api/books/{title}
     */
    @GetMapping("/{title}")
    fun getBook(@PathVariable title: String): ResponseEntity<ApiResponse<BookResponse>> {
        val book = bookService.findByTitle(title)
        return if (book != null) {
            val authors = bookService.getAuthorsForBook(book.title)
            val response = ApiResponse(
                success = true,
                data = BookResponse.from(book, authors),
                message = null
            )
            ResponseEntity.ok(response)
        } else {
            val response = ApiResponse<BookResponse>(
                success = false,
                data = null,
                message = "書籍が見つかりません: ${title}"
            )
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
        }
    }

    /**
     * 書籍一覧を取得する
     * GET /api/books
     */
    @GetMapping
    fun getBooks(
        @RequestParam(required = false) status: PublicationStatus?,
        @RequestParam(required = false) author: String?
    ): ResponseEntity<ApiResponse<List<BookResponse>>> {
        val books = when {
            status != null -> bookService.findByPublicationStatus(status)
            author != null -> bookService.findByAuthorName(author)
            else -> bookService.findAll()
        }
        
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
}