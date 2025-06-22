package test.quo.hardlitchi

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * 最も単純なテスト - フレームワークに依存しない
 */
class SimpleTest {

    @Test
    @DisplayName("単純な計算テスト")
    fun simpleCalculationTest() {
        // Given
        val a = 2
        val b = 3
        
        // When
        val result = a + b
        
        // Then
        assertThat(result).isEqualTo(5)
    }
    
    @Test
    @DisplayName("日付計算テスト")
    fun dateCalculationTest() {
        // 日付ロジックのテスト
        val pastDate = java.time.LocalDate.of(1867, 2, 9)
        val currentDate = java.time.LocalDate.now()
        
        assertThat(pastDate.isBefore(currentDate)).isTrue()
        assertThat(pastDate).isBefore(currentDate)
    }
    
    @Test
    @DisplayName("Authorエンティティのバリデーションテスト")
    fun authorEntityValidationTest() {
        // TDDのフィードバックループが機能するかを確認
        // Entity層のビジネスロジックテスト
        
        // 正常なAuthorエンティティを作成できるかテスト
        val author = test.quo.hardlitchi.common.entity.Author(
            name = "夏目漱石",
            birthDate = java.time.LocalDate.of(2020, 1, 1), // より最近の過去の日付を使用
            createdAt = java.time.LocalDateTime.now(),
            createdBy = "test",
            updatedAt = java.time.LocalDateTime.now(),
            updatedBy = "test"
        )
        
        assertThat(author.name).isEqualTo("夏目漱石")
        assertThat(author.birthDate).isEqualTo(java.time.LocalDate.of(2020, 1, 1))
    }
}