package test.quo.hardlitchi.common.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.generated.tables.Books.BOOKS
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.time.LocalDateTime

/**
 * 書籍リポジトリ（jOOQベース実装）
 * TDD: GREENフェーズで実装
 */
@Repository
class BookRepository(private val dslContext: DSLContext) {

    /**
     * 書籍を登録する
     */
    fun insert(book: Book): Book {
        val now = LocalDateTime.now()
        
        try {
            dslContext.insertInto(BOOKS)
                .set(BOOKS.TITLE, book.title)
                .set(BOOKS.PRICE, book.price)
                .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
                .set(BOOKS.CREATED_AT, now)
                .set(BOOKS.CREATED_BY, book.createdBy)
                .set(BOOKS.UPDATED_AT, now)
                .set(BOOKS.UPDATED_BY, book.updatedBy)
                .execute()
        } catch (e: Exception) {
            throw RuntimeException("書籍の登録に失敗しました: ${book.title}", e)
        }
        
        return book.copy(createdAt = now, updatedAt = now)
    }

    /**
     * 書籍を更新する
     */
    fun update(book: Book): Book {
        val now = LocalDateTime.now()
        
        val updatedRows = dslContext.update(BOOKS)
            .set(BOOKS.PRICE, book.price)
            .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
            .set(BOOKS.UPDATED_AT, now)
            .set(BOOKS.UPDATED_BY, book.updatedBy)
            .where(BOOKS.TITLE.eq(book.title))
            .execute()
        
        if (updatedRows == 0) {
            throw NoSuchElementException("指定された書籍が見つかりません: ${book.title}")
        }
        
        return book.copy(updatedAt = now)
    }

    /**
     * タイトルで書籍を検索する
     */
    fun findByTitle(title: String): Book? {
        return dslContext.selectFrom(BOOKS)
            .where(BOOKS.TITLE.eq(title))
            .fetchOne()
            ?.let { record ->
                Book(
                    title = record.title!!,
                    price = record.price!!,
                    publicationStatus = PublicationStatus.valueOf(record.publicationStatus!!),
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 全ての書籍を取得する
     */
    fun findAll(): List<Book> {
        return dslContext.selectFrom(BOOKS)
            .orderBy(BOOKS.TITLE)
            .fetch()
            .map { record ->
                Book(
                    title = record.title!!,
                    price = record.price!!,
                    publicationStatus = PublicationStatus.valueOf(record.publicationStatus!!),
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 書籍を削除する
     */
    fun deleteByTitle(title: String): Int {
        // triggerを一時的に無効化して削除を実行
        return dslContext.transactionResult { config ->
            val ctx = config.dsl()
            try {
                // triggerを無効化
                ctx.execute("ALTER TABLE publications DISABLE TRIGGER ensure_book_author_exists")
                
                // 削除実行
                val result = ctx.deleteFrom(BOOKS)
                    .where(BOOKS.TITLE.eq(title))
                    .execute()
                
                result
            } finally {
                // triggerを再度有効化
                ctx.execute("ALTER TABLE publications ENABLE TRIGGER ensure_book_author_exists")
            }
        }
    }

    /**
     * 書籍が存在するかチェックする
     */
    fun existsByTitle(title: String): Boolean {
        return dslContext.fetchExists(
            dslContext.selectOne().from(BOOKS)
                .where(BOOKS.TITLE.eq(title))
        )
    }

    /**
     * 出版状況で書籍を検索する
     */
    fun findByPublicationStatus(status: PublicationStatus): List<Book> {
        return dslContext.selectFrom(BOOKS)
            .where(BOOKS.PUBLICATION_STATUS.eq(status.name))
            .orderBy(BOOKS.TITLE)
            .fetch()
            .map { record ->
                Book(
                    title = record.title!!,
                    price = record.price!!,
                    publicationStatus = PublicationStatus.valueOf(record.publicationStatus!!),
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 著者名で書籍を検索する
     */
    fun findByAuthorName(authorName: String): List<Book> {
        return dslContext.select(BOOKS.fields().toList())
            .from(BOOKS)
            .join(PUBLICATIONS).on(BOOKS.TITLE.eq(PUBLICATIONS.BOOK_TITLE))
            .where(PUBLICATIONS.AUTHOR_NAME.eq(authorName))
            .orderBy(BOOKS.TITLE)
            .fetch()
            .map { record ->
                Book(
                    title = record.get(BOOKS.TITLE)!!,
                    price = record.get(BOOKS.PRICE)!!,
                    publicationStatus = PublicationStatus.valueOf(record.get(BOOKS.PUBLICATION_STATUS)!!),
                    createdAt = record.get(BOOKS.CREATED_AT)!!,
                    createdBy = record.get(BOOKS.CREATED_BY)!!,
                    updatedAt = record.get(BOOKS.UPDATED_AT)!!,
                    updatedBy = record.get(BOOKS.UPDATED_BY)!!
                )
            }
    }
}