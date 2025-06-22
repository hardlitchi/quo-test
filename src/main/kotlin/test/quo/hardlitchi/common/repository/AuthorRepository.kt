package test.quo.hardlitchi.common.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.generated.tables.Authors.AUTHORS
import java.time.LocalDateTime

/**
 * 著者リポジトリ（jOOQベース実装）
 * TDDのREFACTOR段階で実装
 */
@Repository
class AuthorRepository(private val dslContext: DSLContext) {

    /**
     * 著者を登録する
     * 
     * @param author 登録する著者エンティティ
     * @return 登録された著者エンティティ（タイムスタンプ更新済み）
     * @throws RuntimeException 登録に失敗した場合
     */
    fun insert(author: Author): Author {
        val now = LocalDateTime.now()
        
        try {
            dslContext.insertInto(AUTHORS)
                .set(AUTHORS.NAME, author.name)
                .set(AUTHORS.BIRTH_DATE, author.birthDate)
                .set(AUTHORS.CREATED_AT, now)
                .set(AUTHORS.CREATED_BY, author.createdBy)
                .set(AUTHORS.UPDATED_AT, now)
                .set(AUTHORS.UPDATED_BY, author.updatedBy)
                .execute()
        } catch (e: Exception) {
            throw RuntimeException("著者の登録に失敗しました: ${author.name}", e)
        }
        
        return author.copy(createdAt = now, updatedAt = now)
    }

    /**
     * 著者を更新する
     * 
     * @param author 更新する著者エンティティ
     * @return 更新された著者エンティティ（タイムスタンプ更新済み）
     * @throws NoSuchElementException 指定された著者が存在しない場合
     */
    fun update(author: Author): Author {
        val now = LocalDateTime.now()
        
        val updatedRows = dslContext.update(AUTHORS)
            .set(AUTHORS.BIRTH_DATE, author.birthDate)
            .set(AUTHORS.UPDATED_AT, now)
            .set(AUTHORS.UPDATED_BY, author.updatedBy)
            .where(AUTHORS.NAME.eq(author.name))
            .execute()
        
        if (updatedRows == 0) {
            throw NoSuchElementException("指定された著者が見つかりません: ${author.name}")
        }
        
        return author.copy(updatedAt = now)
    }

    /**
     * 名前で著者を検索する
     * 
     * @param name 検索する著者名
     * @return 見つかった著者エンティティ、存在しない場合はnull
     */
    fun findByName(name: String): Author? {
        return dslContext.selectFrom(AUTHORS)
            .where(AUTHORS.NAME.eq(name))
            .fetchOne()
            ?.let { record ->
                Author(
                    name = record.name!!,
                    birthDate = record.birthDate!!,
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 全ての著者を取得する
     * 
     * @return 全著者のリスト（名前順でソート）
     */
    fun findAll(): List<Author> {
        return dslContext.selectFrom(AUTHORS)
            .orderBy(AUTHORS.NAME)
            .fetch()
            .map { record ->
                Author(
                    name = record.name!!,
                    birthDate = record.birthDate!!,
                    createdAt = record.createdAt!!,
                    createdBy = record.createdBy!!,
                    updatedAt = record.updatedAt!!,
                    updatedBy = record.updatedBy!!
                )
            }
    }

    /**
     * 著者を削除する
     * 
     * @param name 削除する著者名
     * @return 削除されたレコード数
     */
    fun deleteByName(name: String): Int {
        return dslContext.deleteFrom(AUTHORS)
            .where(AUTHORS.NAME.eq(name))
            .execute()
    }

    /**
     * 著者が存在するかチェックする
     * 
     * @param name チェックする著者名
     * @return 存在する場合true、存在しない場合false
     */
    fun existsByName(name: String): Boolean {
        return dslContext.fetchExists(
            dslContext.selectOne().from(AUTHORS)
                .where(AUTHORS.NAME.eq(name))
        )
    }
}