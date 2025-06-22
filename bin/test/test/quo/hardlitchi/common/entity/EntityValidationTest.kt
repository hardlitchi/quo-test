package test.quo.hardlitchi.common.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Entity validation tests to achieve 100% coverage
 */
class EntityValidationTest {

    private val now = LocalDateTime.now()
    private val pastDate = LocalDate.now().minusYears(30)

    @Test
    fun bookValidationTests() {
        // Empty title
        assertThrows<IllegalArgumentException> {
            Book("", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "system", now, "system")
        }
        
        // Blank title
        assertThrows<IllegalArgumentException> {
            Book("   ", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "system", now, "system")
        }
        
        // Negative price
        assertThrows<IllegalArgumentException> {
            Book("Test Book", BigDecimal("-100"), PublicationStatus.UNPUBLISHED, now, "system", now, "system")
        }
        
        // Empty createdBy
        assertThrows<IllegalArgumentException> {
            Book("Test Book", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "", now, "system")
        }
        
        // Blank createdBy
        assertThrows<IllegalArgumentException> {
            Book("Test Book", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "   ", now, "system")
        }
        
        // Empty updatedBy
        assertThrows<IllegalArgumentException> {
            Book("Test Book", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "system", now, "")
        }
        
        // Blank updatedBy
        assertThrows<IllegalArgumentException> {
            Book("Test Book", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "system", now, "   ")
        }
        
        // Valid case
        val book = Book("Test Book", BigDecimal.ZERO, PublicationStatus.UNPUBLISHED, now, "system", now, "system")
        assertThat(book.title).isEqualTo("Test Book")
        assertThat(book.price).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun authorValidationTests() {
        // Empty name
        assertThrows<IllegalArgumentException> {
            Author("", pastDate, now, "system", now, "system")
        }
        
        // Blank name
        assertThrows<IllegalArgumentException> {
            Author("   ", pastDate, now, "system", now, "system")
        }
        
        // Future birth date
        val futureDate = LocalDate.now().plusDays(1)
        assertThrows<IllegalArgumentException> {
            Author("Test Author", futureDate, now, "system", now, "system")
        }
        
        // Empty createdBy
        assertThrows<IllegalArgumentException> {
            Author("Test Author", pastDate, now, "", now, "system")
        }
        
        // Blank createdBy
        assertThrows<IllegalArgumentException> {
            Author("Test Author", pastDate, now, "   ", now, "system")
        }
        
        // Empty updatedBy
        assertThrows<IllegalArgumentException> {
            Author("Test Author", pastDate, now, "system", now, "")
        }
        
        // Blank updatedBy
        assertThrows<IllegalArgumentException> {
            Author("Test Author", pastDate, now, "system", now, "   ")
        }
        
        // Valid case
        val author = Author("Test Author", pastDate, now, "system", now, "system")
        assertThat(author.name).isEqualTo("Test Author")
        assertThat(author.birthDate).isEqualTo(pastDate)
    }

    @Test
    fun publicationValidationTests() {
        // Empty bookTitle
        assertThrows<IllegalArgumentException> {
            Publication("", "Test Author", now, "system", now, "system")
        }
        
        // Blank bookTitle
        assertThrows<IllegalArgumentException> {
            Publication("   ", "Test Author", now, "system", now, "system")
        }
        
        // Empty authorName
        assertThrows<IllegalArgumentException> {
            Publication("Test Book", "", now, "system", now, "system")
        }
        
        // Blank authorName
        assertThrows<IllegalArgumentException> {
            Publication("Test Book", "   ", now, "system", now, "system")
        }
        
        // Empty createdBy
        assertThrows<IllegalArgumentException> {
            Publication("Test Book", "Test Author", now, "", now, "system")
        }
        
        // Blank createdBy
        assertThrows<IllegalArgumentException> {
            Publication("Test Book", "Test Author", now, "   ", now, "system")
        }
        
        // Empty updatedBy
        assertThrows<IllegalArgumentException> {
            Publication("Test Book", "Test Author", now, "system", now, "")
        }
        
        // Blank updatedBy
        assertThrows<IllegalArgumentException> {
            Publication("Test Book", "Test Author", now, "system", now, "   ")
        }
        
        // Valid case
        val publication = Publication("Test Book", "Test Author", now, "system", now, "system")
        assertThat(publication.bookTitle).isEqualTo("Test Book")
        assertThat(publication.authorName).isEqualTo("Test Author")
    }

    @Test
    fun publicationIdValidationTests() {
        // Empty bookTitle
        assertThrows<IllegalArgumentException> {
            PublicationId("", "Test Author")
        }
        
        // Blank bookTitle
        assertThrows<IllegalArgumentException> {
            PublicationId("   ", "Test Author")
        }
        
        // Empty authorName
        assertThrows<IllegalArgumentException> {
            PublicationId("Test Book", "")
        }
        
        // Blank authorName
        assertThrows<IllegalArgumentException> {
            PublicationId("Test Book", "   ")
        }
        
        // Valid case
        val publicationId = PublicationId("Test Book", "Test Author")
        assertThat(publicationId.bookTitle).isEqualTo("Test Book")
        assertThat(publicationId.authorName).isEqualTo("Test Author")
    }

    @Test
    fun bookStatusChangeTests() {
        val unpublishedBook = Book("Test Book", BigDecimal("1000"), PublicationStatus.UNPUBLISHED, now, "system", now, "system")
        val publishedBook = Book("Test Book", BigDecimal("1000"), PublicationStatus.PUBLISHED, now, "system", now, "system")
        
        // Unpublished to published - allowed
        assertThat(unpublishedBook.canChangeStatusTo(PublicationStatus.PUBLISHED)).isTrue()
        
        // Unpublished to unpublished - allowed
        assertThat(unpublishedBook.canChangeStatusTo(PublicationStatus.UNPUBLISHED)).isTrue()
        
        // Published to published - allowed
        assertThat(publishedBook.canChangeStatusTo(PublicationStatus.PUBLISHED)).isTrue()
        
        // Published to unpublished - not allowed
        assertThat(publishedBook.canChangeStatusTo(PublicationStatus.UNPUBLISHED)).isFalse()
    }
}