package test.quo.hardlitchi.web.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

/**
 * GlobalExceptionHandlerのテスト
 * 100%カバレッジを達成するための専用テスト
 */
class GlobalExceptionHandlerTest {

    private val globalExceptionHandler = GlobalExceptionHandler()

    @Test
    @DisplayName("FieldErrorのdefaultMessageがnullの場合のハンドリングをテストする")
    fun handlesValidationExceptionWithNullDefaultMessage() {
        // Given: FieldErrorのdefaultMessageがnullのケースを作成
        val fieldError = mock<FieldError>()
        whenever(fieldError.field).thenReturn("testField")
        whenever(fieldError.defaultMessage).thenReturn(null) // 未カバー箇所をテスト
        
        val bindingResult = mock<org.springframework.validation.BindingResult>()
        whenever(bindingResult.fieldErrors).thenReturn(listOf(fieldError))
        
        val parameter = mock<org.springframework.core.MethodParameter>()
        val exception = MethodArgumentNotValidException(parameter, bindingResult)

        // When: 例外ハンドリングを実行
        val response = globalExceptionHandler.handleValidationException(exception)

        // Then: デフォルトメッセージが使用されることを確認
        assertThat(response.statusCode.value()).isEqualTo(400)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.message).contains("入力値にエラーがあります")
        assertThat(response.body!!.errors).hasSize(1)
        assertThat(response.body!!.errors!![0].field).isEqualTo("testField")
        assertThat(response.body!!.errors!![0].message).isEqualTo("バリデーションエラー")
    }
}