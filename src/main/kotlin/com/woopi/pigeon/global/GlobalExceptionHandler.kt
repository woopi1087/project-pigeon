package com.woopi.pigeon.global

import com.woopi.pigeon.channel.fcm.UnregisteredTokenException
import com.woopi.pigeon.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val message = e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR", message))
    }

    @ExceptionHandler(UnregisteredTokenException::class)
    fun handleUnregisteredToken(e: UnregisteredTokenException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.badRequest().body(ApiResponse.error("TOKEN_UNREGISTERED", e.message ?: "만료된 FCM 토큰"))
    }

    @ExceptionHandler(UnsupportedOperationException::class)
    fun handleUnsupportedChannel(e: UnsupportedOperationException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.badRequest().body(ApiResponse.error("UNSUPPORTED_CHANNEL", e.message ?: "지원하지 않는 채널"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", e.message ?: "서버 오류"))
    }
}
