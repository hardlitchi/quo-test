package test.quo.hardlitchi.common.repository

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import test.quo.hardlitchi.common.entity.Author
import test.quo.hardlitchi.generated.tables.AUTHORS
import java.time.LocalDateTime

/**
 * 著者リポジトリ
 */
@Repository
class AuthorRepository(private val dslContext: DSLContext) {

    /**
     * 著者を登録する
     * 
     * @param author 登録する著者情報
     * @return 登録された著者情報
     */
    fun insert(author: Author): Author {
        val now = LocalDateTime.now()
        
        dslContext.insertInto(AUTHORS.AUTHORS)
            .set(AUTHORS.AUTHORS.NAME, author.name)
            .set(AUTHORS.AUTHORS.BIRTH_DATE, author.birthDate)
            .set(AUTHORS.AUTHORS.CREATED_AT, now)
            .set(AUTHORS.AUTHORS.CREATED_BY, author.createdBy)
            .set(AUTHORS.AUTHORS.UPDATED_AT, now)
            .set(AUTHORS.AUTHORS.UPDATED_BY, author.updatedBy)
            .execute()
            
        return author.copy(createdAt = now, updatedAt = now)
    }

    /**
     * 著者を更新する
     * 
     * @param author 更新する著者情報
     * @return 更新された著者情報
     */
    fun update(author: Author): Author {
        val now = LocalDateTime.now()
        
        val updatedRows = dslContext.update(AUTHORS.AUTHORS)
            .set(AUTHORS.AUTHORS.BIRTH_DATE, author.birthDate)
            .set(AUTHORS.AUTHORS.UPDATED_AT, now)
            .set(AUTHORS.AUTHORS.UPDATED_BY, author.updatedBy)
            .where(AUTHORS.AUTHORS.NAME.eq(author.name))
            .execute()
            
        if (updatedRows == 0) {
            throw NoSuchElementException("指定された著者が見つかりません: ${author.name}")
        }
        
        return author.copy(updatedAt = now)
    }

    /**
     * 著者名で著者を検索する
     * 
     * @param name 著者名
     * @return 著者情報（見つからない場合はnull）
     */
    fun findByName(name: String): Author? {
        return dslContext.selectFrom(AUTHORS.AUTHORS)
            .where(AUTHORS.AUTHORS.NAME.eq(name))
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
     * @return 著者のリスト
     */
    fun findAll(): List<Author> {
        return dslContext.selectFrom(AUTHORS.AUTHORS)
            .orderBy(AUTHORS.AUTHORS.NAME)
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
     * @return 削除された行数
     */
    fun deleteByName(name: String): Int {
        return dslContext.deleteFrom(AUTHORS.AUTHORS)
            .where(AUTHORS.AUTHORS.NAME.eq(name))
            .execute()
    }

    /**
     * 著者が存在するかチェックする
     * 
     * @param name 著者名
     * @return 存在する場合true
     */
    fun existsByName(name: String): Boolean {
        return dslContext.fetchExists(
            dslContext.selectOne().from(AUTHORS.AUTHORS)
                .where(AUTHORS.AUTHORS.NAME.eq(name))
        )
    }
}