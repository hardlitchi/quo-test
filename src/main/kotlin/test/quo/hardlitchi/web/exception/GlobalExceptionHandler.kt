package test.quo.hardlitchi.web.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import test.quo.hardlitchi.common.service.DuplicateResourceException
import test.quo.hardlitchi.common.service.ResourceNotFoundException
import test.quo.hardlitchi.web.bean.ApiResponse
import test.quo.hardlitchi.web.bean.FieldError

/**
 * グローバル例外ハンドラー
 * 全てのControllerで発生する例外を統一的に処理
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * バリデーションエラー処理
     * 
     * @param e メソッド引数バリデーション例外
     * @return バリデーションエラーのレスポンス（400 Bad Request）
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any>> {
        val errors = e.bindingResult.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "バリデーションエラー"
            )
        }
        
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = "入力値にエラーがあります",
            errors = errors
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * リソース未発見例外処理
     * 
     * @param e リソース未発見例外
     * @return リソース未発見エラーのレスポンス（404 Not Found）
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(e: ResourceNotFoundException): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = e.message
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * 重複リソース例外処理
     * 
     * @param e 重複リソース例外
     * @return 重複リソースエラーのレスポンス（409 Conflict）
     */
    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(e: DuplicateResourceException): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = e.message
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    /**
     * 引数例外処理
     * 
     * @param e 不正引数例外
     * @return 不正引数エラーのレスポンス（400 Bad Request）
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = e.message
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * JSONパースエラー処理
     * 
     * @param e HTTPメッセージ読み取り不可例外
     * @return JSONパースエラーのレスポンス（400 Bad Request）
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = "リクエストの形式が正しくありません"
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    /**
     * Content-Type不正エラー処理
     * 
     * @param e HTTPメディアタイプ未サポート例外
     * @return Content-Typeエラーのレスポンス（415 Unsupported Media Type）
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = "サポートされていないContent-Typeです"
        )
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response)
    }

    /**
     * その他の例外処理（サーバーエラー）
     * 
     * @param e 予期しない例外
     * @return サーバーエラーのレスポンス（500 Internal Server Error）
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ApiResponse<Any>> {
        val response = ApiResponse<Any>(
            success = false,
            data = null,
            message = "サーバーエラーが発生しました"
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}