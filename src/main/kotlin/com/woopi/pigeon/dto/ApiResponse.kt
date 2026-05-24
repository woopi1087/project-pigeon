package com.woopi.pigeon.dto

sealed class ApiResponse<out T> {

    data class Success<T>(val data: T? = null, val message: String? = null) : ApiResponse<T>()
    data class Error(val code: String, val message: String) : ApiResponse<Nothing>()

    companion object {
        fun success(message: String? = null): ApiResponse<Unit> = Success(message = message)
        fun <T> success(data: T): ApiResponse<T> = Success(data = data)
        fun error(code: String, message: String): ApiResponse<Nothing> = Error(code, message)
    }
}
