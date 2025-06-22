package test.quo.hardlitchi.common.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.quo.hardlitchi.common.entity.Book
import test.quo.hardlitchi.common.entity.PublicationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Service層のDTOクラスのテスト
 * BookWithAuthorsDtoおよび他のDTOクラスの完全カバレッジテスト
 */
class BookWithAuthorsDtoTest {

    @Test
    fun `BookWithAuthorsDtoを正常に作成できる`() {
        val book = Book(
            title = "テスト書籍",
            price = BigDecimal("1500"),
            publicationStatus = PublicationStatus.PUBLISHED,
            createdAt = LocalDateTime.now(),
            createdBy = "system",
            updatedAt = LocalDateTime.now(),
            updatedBy = "system"
        )
        
        val authors = listOf("著者1", "著者2", "著者3")
        
        val dto = BookWithAuthorsDto(
            book = book,
            authors = authors
        )
        
        assertThat(dto.book.title).isEqualTo(book.title)
        assertThat(dto.book.price).isEqualTo(book.price)
        assertThat(dto.book.publicationStatus).isEqualTo(book.publicationStatus)
        assertThat(dto.authors).isEqualTo(authors)
        assertThat(dto.authors.size).isEqualTo(3)
    }

    @Test
    fun `著者リストが空の場合でもBookWithAuthorsDtoを作成できる`() {
        val book = Book(
            title = "著者なし書籍",
            price = BigDecimal("1000"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            createdAt = LocalDateTime.now(),
            createdBy = "system",
            updatedAt = LocalDateTime.now(),
            updatedBy = "system"
        )
        
        val dto = BookWithAuthorsDto(
            book = book,
            authors = emptyList()
        )
        
        assertThat(dto.book.title).isEqualTo(book.title)
        assertThat(dto.authors).isEqualTo(emptyList<String>())
        assertThat(dto.authors.size).isEqualTo(0)
    }

    @Test
    fun `単一著者のBookWithAuthorsDtoを作成できる`() {
        val book = Book(
            title = "単一著者書籍",
            price = BigDecimal("2000"),
            publicationStatus = PublicationStatus.PUBLISHED,
            createdAt = LocalDateTime.now(),
            createdBy = "system",
            updatedAt = LocalDateTime.now(),
            updatedBy = "system"
        )
        
        val authors = listOf("単一著者")
        
        val dto = BookWithAuthorsDto(
            book = book,
            authors = authors
        )
        
        assertThat(dto.book.title).isEqualTo(book.title)
        assertThat(dto.authors).isEqualTo(listOf("単一著者"))
        assertThat(dto.authors.size).isEqualTo(1)
    }

    @Test
    fun `CreateBookDtoの全Getterメソッドをテストする`() {
        // 未カバーのGetterメソッドをテストしてカバレッジを向上
        val dto = CreateBookDto(
            title = "こころ",
            price = BigDecimal("500"),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf("夏目漱石"),
            createdBy = "testUser"
        )

        // 未カバーメソッドのテスト
        assertThat(dto.publicationStatus).isEqualTo(PublicationStatus.PUBLISHED)
        assertThat(dto.createdBy).isEqualTo("testUser")
        
        // その他のフィールドもテスト
        assertThat(dto.title).isEqualTo("こころ")
        assertThat(dto.price).isEqualByComparingTo(BigDecimal("500"))
        assertThat(dto.authors).containsExactly("夏目漱石")
    }

    @Test
    fun `UpdateBookDtoの全Getterメソッドをテストする`() {
        // 未カバーのGetterメソッドをテストしてカバレッジを向上
        val dto = UpdateBookDto(
            title = "こころ",
            price = BigDecimal("600"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf("夏目漱石"),
            updatedBy = "testUser"
        )

        // 未カバーメソッドのテスト
        assertThat(dto.updatedBy).isEqualTo("testUser")
        
        // その他のフィールドもテスト
        assertThat(dto.title).isEqualTo("こころ")
        assertThat(dto.price).isEqualByComparingTo(BigDecimal("600"))
        assertThat(dto.publicationStatus).isEqualTo(PublicationStatus.UNPUBLISHED)
        assertThat(dto.authors).containsExactly("夏目漱石")
    }

    @Test
    fun `CreateAuthorDtoの全Getterメソッドをテストする`() {
        // 未カバーのGetterメソッドをテストしてカバレッジを向上
        val dto = CreateAuthorDto(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9),
            createdBy = "testUser"
        )

        // 未カバーメソッドのテスト
        assertThat(dto.createdBy).isEqualTo("testUser")
        
        // その他のフィールドもテスト
        assertThat(dto.name).isEqualTo("夏目漱石")
        assertThat(dto.birthDate).isEqualTo(LocalDate.of(1867, 2, 9))
    }
}