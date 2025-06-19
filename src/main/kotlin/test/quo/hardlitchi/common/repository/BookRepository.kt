package test.quo.hardlitchi.common.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.generated.tables.BOOKS
import java.time.LocalDateTime

/**
 * 書籍リポジトリ
 */
@Repository
class BookRepository(private val dslContext: DSLContext) {

    /**
     * 書籍を登録する
     * 
     * @param book 登録する書籍情報
     * @return 登録された書籍情報
     */
    fun insert(book: Book): Book {
        val now = LocalDateTime.now()
        
        dslContext.insertInto(BOOKS.BOOKS)
            .set(BOOKS.BOOKS.TITLE, book.title)
            .set(BOOKS.BOOKS.PRICE, book.price)
            .set(BOOKS.BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
            .set(BOOKS.BOOKS.CREATED_AT, now)
            .set(BOOKS.BOOKS.CREATED_BY, book.createdBy)
            .set(BOOKS.BOOKS.UPDATED_AT, now)
            .set(BOOKS.BOOKS.UPDATED_BY, book.updatedBy)
            .execute()
            
        return book.copy(createdAt = now, updatedAt = now)
    }

    /**
     * 書籍を更新する
     * 
     * @param book 更新する書籍情報
     * @return 更新された書籍情報
     * @throws IllegalStateException 出版済み書籍を未出版に戻そうとした場合
     */
    fun update(book: Book): Book {
        // 既存の書籍情報を取得して状態変更をチェック
        val existingBook = findByTitle(book.title)
            ?: throw NoSuchElementException("指定された書籍が見つかりません: ${book.title}")
            
        if (!existingBook.canChangeStatusTo(book.publicationStatus)) {
            throw IllegalStateException("出版済みの書籍を未出版状態に戻すことはできません: ${book.title}")
        }
        
        val now = LocalDateTime.now()
        
        val updatedRows = dslContext.update(BOOKS.BOOKS)
            .set(BOOKS.BOOKS.PRICE, book.price)
            .set(BOOKS.BOOKS.PUBLICATION_STATUS, book.publicationStatus.name)
            .set(BOOKS.BOOKS.UPDATED_AT, now)
            .set(BOOKS.BOOKS.UPDATED_BY, book.updatedBy)
            .where(BOOKS.BOOKS.TITLE.eq(book.title))
            .execute()
            
        if (updatedRows == 0) {
            throw NoSuchElementException("指定された書籍が見つかりません: ${book.title}")
        }
        
        return book.copy(updatedAt = now)
    }

    /**
     * タイトルで書籍を検索する
     * 
     * @param title 書籍タイトル
     * @return 書籍情報（見つからない場合はnull）
     */
    fun findByTitle(title: String): Book? {
        return dslContext.selectFrom(BOOKS.BOOKS)
            .where(BOOKS.BOOKS.TITLE.eq(title))
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
     * 
     * @return 書籍のリスト
     */
    fun findAll(): List<Book> {
        return dslContext.selectFrom(BOOKS.BOOKS)
            .orderBy(BOOKS.BOOKS.TITLE)
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
     * 出版状況で書籍を検索する
     * 
     * @param status 出版状況
     * @return 該当する書籍のリスト
     */
    fun findByPublicationStatus(status: PublicationStatus): List<Book> {
        return dslContext.selectFrom(BOOKS.BOOKS)
            .where(BOOKS.BOOKS.PUBLICATION_STATUS.eq(status.name))
            .orderBy(BOOKS.BOOKS.TITLE)
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
     * 
     * @param title 削除する書籍タイトル
     * @return 削除された行数
     */
    fun deleteByTitle(title: String): Int {
        return dslContext.deleteFrom(BOOKS.BOOKS)
            .where(BOOKS.BOOKS.TITLE.eq(title))
            .execute()
    }

    /**
     * 書籍が存在するかチェックする
     * 
     * @param title 書籍タイトル
     * @return 存在する場合true
     */
    fun existsByTitle(title: String): Boolean {
        return dslContext.fetchExists(
            dslContext.selectOne().from(BOOKS.BOOKS)
                .where(BOOKS.BOOKS.TITLE.eq(title))
        )
    }
}