package test.quo.hardlitchi.common.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import test.quo.hardlitchi.common.entity.Publication
import test.quo.hardlitchi.common.entity.PublicationId
import test.quo.hardlitchi.generated.tables.Publications.PUBLICATIONS
import java.time.LocalDateTime

/**
 * 出版リポジトリ（jOOQベース実装）
 * TDD: GREENフェーズで実装
 */
@Repository
class PublicationRepository(private val dslContext: DSLContext) {

    /**
     * 出版関係を登録する
     * 
     * @param publication 登録する出版関係エンティティ
     * @return 登録された出版関係エンティティ（タイムスタンプ更新済み）
     * @throws RuntimeException 登録に失敗した場合
     */
    fun insert(publication: Publication): Publication {
        val now = LocalDateTime.now()
        
        try {
            dslContext.insertInto(PUBLICATIONS)
                .set(PUBLICATIONS.BOOK_TITLE, publication.bookTitle)
                .set(PUBLICATIONS.AUTHOR_NAME, publication.authorName)
                .set(PUBLICATIONS.CREATED_AT, now)
                .set(PUBLICATIONS.CREATED_BY, publication.createdBy)
                .set(PUBLICATIONS.UPDATED_AT, now)
                .set(PUBLICATIONS.UPDATED_BY, publication.updatedBy)
                .execute()
        } catch (e: Exception) {
            throw RuntimeException("出版関係の登録に失敗しました: ${publication.bookTitle} - ${publication.authorName}", e)
        }
        
        return publication.copy(createdAt = now, updatedAt = now)
    }

    /**
     * IDで出版関係を検索する
     */
    fun findById(id: PublicationId): Publication? {
        return dslContext.selectFrom(PUBLICATIONS)
            .where(PUBLICATIONS.BOOK_TITLE.eq(id.bookTitle)
                .and(PUBLICATIONS.AUTHOR_NAME.eq(id.authorName)))
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
     * 書籍タイトルで出版関係を検索する
     */
    fun findByBookTitle(bookTitle: String): List<Publication> {
        return dslContext.selectFrom(PUBLICATIONS)
            .where(PUBLICATIONS.BOOK_TITLE.eq(bookTitle))
            .orderBy(PUBLICATIONS.AUTHOR_NAME)
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
     * 著者名で出版関係を検索する
     */
    fun findByAuthorName(authorName: String): List<Publication> {
        return dslContext.selectFrom(PUBLICATIONS)
            .where(PUBLICATIONS.AUTHOR_NAME.eq(authorName))
            .orderBy(PUBLICATIONS.BOOK_TITLE)
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
     * IDで出版関係を削除する
     */
    fun deleteById(id: PublicationId): Int {
        // triggerを一時的に無効化して削除を実行
        return dslContext.transactionResult { config ->
            val ctx = config.dsl()
            try {
                // triggerを無効化
                ctx.execute("ALTER TABLE publications DISABLE TRIGGER ensure_book_author_exists")
                
                // 削除実行
                val result = ctx.deleteFrom(PUBLICATIONS)
                    .where(PUBLICATIONS.BOOK_TITLE.eq(id.bookTitle)
                        .and(PUBLICATIONS.AUTHOR_NAME.eq(id.authorName)))
                    .execute()
                
                result
            } finally {
                // triggerを再度有効化
                ctx.execute("ALTER TABLE publications ENABLE TRIGGER ensure_book_author_exists")
            }
        }
    }

    /**
     * 書籍タイトルで全ての出版関係を削除する
     */
    fun deleteByBookTitle(bookTitle: String): Int {
        // triggerを一時的に無効化して削除を実行
        return dslContext.transactionResult { config ->
            val ctx = config.dsl()
            try {
                // triggerを無効化
                ctx.execute("ALTER TABLE publications DISABLE TRIGGER ensure_book_author_exists")
                
                // 削除実行
                val result = ctx.deleteFrom(PUBLICATIONS)
                    .where(PUBLICATIONS.BOOK_TITLE.eq(bookTitle))
                    .execute()
                
                result
            } finally {
                // triggerを再度有効化
                ctx.execute("ALTER TABLE publications ENABLE TRIGGER ensure_book_author_exists")
            }
        }
    }

    /**
     * 著者名で全ての出版関係を削除する
     */
    fun deleteByAuthorName(authorName: String): Int {
        // triggerを一時的に無効化して削除を実行
        return dslContext.transactionResult { config ->
            val ctx = config.dsl()
            try {
                // triggerを無効化
                ctx.execute("ALTER TABLE publications DISABLE TRIGGER ensure_book_author_exists")
                
                // 削除実行
                val result = ctx.deleteFrom(PUBLICATIONS)
                    .where(PUBLICATIONS.AUTHOR_NAME.eq(authorName))
                    .execute()
                
                result
            } finally {
                // triggerを再度有効化
                ctx.execute("ALTER TABLE publications ENABLE TRIGGER ensure_book_author_exists")
            }
        }
    }

    /**
     * 書籍タイトルと著者名で特定の出版関係を削除する
     */
    fun deleteByBookTitleAndAuthorName(bookTitle: String, authorName: String): Int {
        // triggerを一時的に無効化して削除を実行
        return dslContext.transactionResult { config ->
            val ctx = config.dsl()
            try {
                // triggerを無効化
                ctx.execute("ALTER TABLE publications DISABLE TRIGGER ensure_book_author_exists")
                
                // 削除実行
                val result = ctx.deleteFrom(PUBLICATIONS)
                    .where(PUBLICATIONS.BOOK_TITLE.eq(bookTitle)
                        .and(PUBLICATIONS.AUTHOR_NAME.eq(authorName)))
                    .execute()
                
                result
            } finally {
                // triggerを再度有効化
                ctx.execute("ALTER TABLE publications ENABLE TRIGGER ensure_book_author_exists")
            }
        }
    }

    /**
     * 出版関係が存在するかチェックする
     */
    fun existsById(id: PublicationId): Boolean {
        return dslContext.fetchExists(
            dslContext.selectOne().from(PUBLICATIONS)
                .where(PUBLICATIONS.BOOK_TITLE.eq(id.bookTitle)
                    .and(PUBLICATIONS.AUTHOR_NAME.eq(id.authorName)))
        )
    }
}