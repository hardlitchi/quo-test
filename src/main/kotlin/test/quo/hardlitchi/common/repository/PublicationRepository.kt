package test.quo.hardlitchi.common.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.Publication
import test.quo.hardlitchi.common.entity.PublicationId
import test.quo.hardlitchi.common.entity.PublicationStatus
import test.quo.hardlitchi.generated.tables.AUTHORS
import test.quo.hardlitchi.generated.tables.BOOKS
import test.quo.hardlitchi.generated.tables.PUBLICATIONS
import java.time.LocalDateTime

/**
 * 出版リポジトリ
 */
@Repository
class PublicationRepository(private val dslContext: DSLContext) {

    /**
     * 出版情報を登録する
     * 
     * @param publication 登録する出版情報
     * @return 登録された出版情報
     */
    fun insert(publication: Publication): Publication {
        val now = LocalDateTime.now()
        
        dslContext.insertInto(PUBLICATIONS.PUBLICATIONS)
            .set(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE, publication.bookTitle)
            .set(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME, publication.authorName)
            .set(PUBLICATIONS.PUBLICATIONS.CREATED_AT, now)
            .set(PUBLICATIONS.PUBLICATIONS.CREATED_BY, publication.createdBy)
            .set(PUBLICATIONS.PUBLICATIONS.UPDATED_AT, now)
            .set(PUBLICATIONS.PUBLICATIONS.UPDATED_BY, publication.updatedBy)
            .execute()
            
        return publication.copy(createdAt = now, updatedAt = now)
    }

    /**
     * 出版情報を更新する
     * 
     * @param publication 更新する出版情報
     * @return 更新された出版情報
     */
    fun update(publication: Publication): Publication {
        val now = LocalDateTime.now()
        
        val updatedRows = dslContext.update(PUBLICATIONS.PUBLICATIONS)
            .set(PUBLICATIONS.PUBLICATIONS.UPDATED_AT, now)
            .set(PUBLICATIONS.PUBLICATIONS.UPDATED_BY, publication.updatedBy)
            .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(publication.bookTitle))
            .and(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.eq(publication.authorName))
            .execute()
            
        if (updatedRows == 0) {
            throw NoSuchElementException("指定された出版情報が見つかりません: ${publication.bookTitle} - ${publication.authorName}")
        }
        
        return publication.copy(updatedAt = now)
    }

    /**
     * 複合主キーで出版情報を検索する
     * 
     * @param publicationId 複合主キー
     * @return 出版情報（見つからない場合はnull）
     */
    fun findById(publicationId: PublicationId): Publication? {
        return dslContext.selectFrom(PUBLICATIONS.PUBLICATIONS)
            .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(publicationId.bookTitle))
            .and(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.eq(publicationId.authorName))
            .fetchOne()
            ?.let { record ->
                Publication(
                    bookTitle = record.bookTitle!!,
                    authorName = record.authorName!!,
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 書籍タイトルで出版情報を検索する
     * 
     * @param bookTitle 書籍タイトル
     * @return 該当する出版情報のリスト
     */
    fun findByBookTitle(bookTitle: String): List<Publication> {
        return dslContext.selectFrom(PUBLICATIONS.PUBLICATIONS)
            .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(bookTitle))
            .orderBy(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME)
            .fetch()
            .map { record ->
                Publication(
                    bookTitle = record.bookTitle!!,
                    authorName = record.authorName!!,
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 著者名で出版情報を検索する
     * 
     * @param authorName 著者名
     * @return 該当する出版情報のリスト
     */
    fun findByAuthorName(authorName: String): List<Publication> {
        return dslContext.selectFrom(PUBLICATIONS.PUBLICATIONS)
            .where(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.eq(authorName))
            .orderBy(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE)
            .fetch()
            .map { record ->
                Publication(
                    bookTitle = record.bookTitle!!,
                    authorName = record.authorName!!,
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 著者が執筆した書籍一覧を取得する
     * 
     * @param authorName 著者名
     * @return 該当する書籍のリスト
     */
    fun findBooksByAuthor(authorName: String): List<Book> {
        return dslContext.select()
            .from(BOOKS.BOOKS)
            .join(PUBLICATIONS.PUBLICATIONS)
            .on(BOOKS.BOOKS.TITLE.eq(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE))
            .where(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.eq(authorName))
            .orderBy(BOOKS.BOOKS.TITLE)
            .fetch()
            .map { record ->
                Book(
                    title = record[BOOKS.BOOKS.TITLE]!!,
                    price = record[BOOKS.BOOKS.PRICE]!!,
                    publicationStatus = PublicationStatus.valueOf(record[BOOKS.BOOKS.PUBLICATION_STATUS]!!),
                    createdAt = record[BOOKS.BOOKS.CREATED_AT]!!,
                    createdBy = record[BOOKS.BOOKS.CREATED_BY]!!,
                    updatedAt = record[BOOKS.BOOKS.UPDATED_AT]!!,
                    updatedBy = record[BOOKS.BOOKS.UPDATED_BY]!!
                )
            }
    }

    /**
     * 書籍の著者一覧を取得する
     * 
     * @param bookTitle 書籍タイトル
     * @return 該当する著者のリスト
     */
    fun findAuthorsByBook(bookTitle: String): List<Author> {
        return dslContext.select()
            .from(AUTHORS.AUTHORS)
            .join(PUBLICATIONS.PUBLICATIONS)
            .on(AUTHORS.AUTHORS.NAME.eq(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME))
            .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(bookTitle))
            .orderBy(AUTHORS.AUTHORS.NAME)
            .fetch()
            .map { record ->
                Author(
                    name = record[AUTHORS.AUTHORS.NAME]!!,
                    birthDate = record[AUTHORS.AUTHORS.BIRTH_DATE]!!,
                    createdAt = record[AUTHORS.AUTHORS.CREATED_AT]!!,
                    createdBy = record[AUTHORS.AUTHORS.CREATED_BY]!!,
                    updatedAt = record[AUTHORS.AUTHORS.UPDATED_AT]!!,
                    updatedBy = record[AUTHORS.AUTHORS.UPDATED_BY]!!
                )
            }
    }

    /**
     * 出版情報を削除する
     * 
     * @param publicationId 削除する出版情報の複合主キー
     * @return 削除された行数
     * @throws IllegalStateException 書籍の最後の著者を削除しようとした場合
     */
    fun deleteById(publicationId: PublicationId): Int {
        // 書籍に他の著者が存在するかチェック
        val otherAuthorsCount = dslContext.selectCount()
            .from(PUBLICATIONS.PUBLICATIONS)
            .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(publicationId.bookTitle))
            .and(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.ne(publicationId.authorName))
            .fetchOne(0, Int::class.java) ?: 0
            
        if (otherAuthorsCount == 0) {
            throw IllegalStateException("書籍には最低1人の著者が必要です。書籍タイトル: ${publicationId.bookTitle}")
        }
        
        return dslContext.deleteFrom(PUBLICATIONS.PUBLICATIONS)
            .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(publicationId.bookTitle))
            .and(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.eq(publicationId.authorName))
            .execute()
    }

    /**
     * 出版情報が存在するかチェックする
     * 
     * @param publicationId 複合主キー
     * @return 存在する場合true
     */
    fun existsById(publicationId: PublicationId): Boolean {
        return dslContext.fetchExists(
            dslContext.selectOne().from(PUBLICATIONS.PUBLICATIONS)
                .where(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE.eq(publicationId.bookTitle))
                .and(PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME.eq(publicationId.authorName))
        )
    }

    /**
     * 全ての出版情報を取得する
     * 
     * @return 出版情報のリスト
     */
    fun findAll(): List<Publication> {
        return dslContext.selectFrom(PUBLICATIONS.PUBLICATIONS)
            .orderBy(PUBLICATIONS.PUBLICATIONS.BOOK_TITLE, PUBLICATIONS.PUBLICATIONS.AUTHOR_NAME)
            .fetch()
            .map { record ->
                Publication(
                    bookTitle = record.bookTitle!!,
                    authorName = record.authorName!!,
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }
}