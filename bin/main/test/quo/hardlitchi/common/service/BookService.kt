package test.quo.hardlitchi.common.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.Publication
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.common.repository.BookRepository
import test.quo.hardlitchi.common.repository.PublicationRepository
import test.quo.hardlitchi.common.repository.AuthorRepository
import java.math.BigDecimal

/**
 * 書籍サービス
 * ビジネスロジックとトランザクション管理を担当
 */
@Service
@Transactional
class BookService(
    private val bookRepository: BookRepository,
    private val publicationRepository: PublicationRepository,
    private val authorRepository: AuthorRepository
) {

    /**
     * 書籍を作成する
     */
    fun createBook(dto: CreateBookDto): Book {
        // バリデーション
        validateBookDto(dto.title, dto.price, dto.authors)
        
        // 重複チェック
        if (bookRepository.existsByTitle(dto.title)) {
            throw DuplicateResourceException("書籍は既に存在します: ${dto.title}")
        }
        
        // 著者の存在チェック
        dto.authors.forEach { authorName ->
            if (!authorRepository.existsByName(authorName)) {
                throw ResourceNotFoundException("著者が見つかりません: ${authorName}")
            }
        }
        
        // 書籍を保存
        val book = bookRepository.insert(dto.toEntity())
        
        // 出版関係を保存
        dto.toPublications().forEach { publication ->
            publicationRepository.insert(publication)
        }
        
        return book
    }

    /**
     * 書籍を更新する
     */
    fun updateBook(dto: UpdateBookDto): Book {
        // バリデーション
        validateBookDto(dto.title, dto.price, dto.authors)
        
        // 存在チェック
        val existingBook = bookRepository.findByTitle(dto.title)
            ?: throw ResourceNotFoundException("書籍が見つかりません: ${dto.title}")
        
        // 出版状況のビジネスルールチェック
        if (!existingBook.canChangeStatusTo(dto.publicationStatus)) {
            throw IllegalArgumentException("出版済み書籍を未出版に戻すことはできません")
        }
        
        // 著者の存在チェック
        dto.authors.forEach { authorName ->
            if (!authorRepository.existsByName(authorName)) {
                throw ResourceNotFoundException("著者が見つかりません: ${authorName}")
            }
        }
        
        // 書籍を更新
        val updatedBook = bookRepository.update(dto.updateEntity(existingBook))
        
        // 出版関係を更新（一度削除して再登録）
        val currentPublications = publicationRepository.findByBookTitle(dto.title)
        
        // 新しい出版関係を先に登録してから古いものを削除
        val newPublications = dto.toPublications()
        newPublications.forEach { publication ->
            // 既存に含まれていない場合のみ追加
            if (!currentPublications.any { it.authorName == publication.authorName }) {
                publicationRepository.insert(publication)
            }
        }
        
        // 新しいリストに含まれていない古い出版関係を削除
        currentPublications.forEach { currentPub ->
            if (!newPublications.any { it.authorName == currentPub.authorName }) {
                publicationRepository.deleteByBookTitleAndAuthorName(dto.title, currentPub.authorName)
            }
        }
        
        return updatedBook
    }

    /**
     * 書籍をタイトルで検索する
     */
    @Transactional(readOnly = true)
    fun findByTitle(title: String): Book? {
        return bookRepository.findByTitle(title)
    }

    /**
     * 全ての書籍を取得する
     */
    @Transactional(readOnly = true)
    fun findAll(): List<Book> {
        return bookRepository.findAll()
    }

    /**
     * 出版状況で書籍を検索する
     */
    @Transactional(readOnly = true)
    fun findByPublicationStatus(status: PublicationStatus): List<Book> {
        return bookRepository.findByPublicationStatus(status)
    }

    /**
     * 著者名で書籍を検索する
     */
    @Transactional(readOnly = true)
    fun findByAuthorName(authorName: String): List<Book> {
        return bookRepository.findByAuthorName(authorName)
    }

    /**
     * 書籍の著者一覧を取得する
     */
    @Transactional(readOnly = true)
    fun getAuthorsForBook(bookTitle: String): List<String> {
        return publicationRepository.findByBookTitle(bookTitle)
            .map { it.authorName }
            .sorted()
    }

    /**
     * 書籍を削除する
     */
    fun deleteByTitle(title: String) {
        // 存在チェック
        if (!bookRepository.existsByTitle(title)) {
            throw ResourceNotFoundException("書籍が見つかりません: ${title}")
        }
        
        // CASCADE制約により、書籍を削除すれば出版関係も自動削除される
        bookRepository.deleteByTitle(title)
    }

    /**
     * 書籍が存在するかチェックする
     */
    @Transactional(readOnly = true)
    fun existsByTitle(title: String): Boolean {
        return bookRepository.existsByTitle(title)
    }

    /**
     * 書籍DTOのバリデーション
     */
    private fun validateBookDto(title: String, price: BigDecimal, authors: List<String>) {
        if (title.isBlank()) {
            throw IllegalArgumentException("書籍タイトルは必須です")
        }
        
        if (price < BigDecimal.ZERO) {
            throw IllegalArgumentException("価格は0以上である必要があります")
        }
        
        if (authors.isEmpty()) {
            throw IllegalArgumentException("書籍には最低1人の著者が必要です")
        }
        
        authors.forEach { authorName ->
            if (authorName.isBlank()) {
                throw IllegalArgumentException("著者名は必須です")
            }
        }
    }
}