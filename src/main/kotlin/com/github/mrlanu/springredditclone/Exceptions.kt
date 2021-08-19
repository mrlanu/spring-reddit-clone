package com.github.mrlanu.springredditclone

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

class ErrorMessage(
    val error: Int,
    val message: String,
    val time: LocalDateTime
)

class ResourceNotFoundException(msg: String): RuntimeException(msg)

@ControllerAdvice
class ControllerExceptionsHandler{

    @ExceptionHandler(ResourceNotFoundException::class)
    fun resourceNotFoundExceptionHandler(ex: ResourceNotFoundException,
                                  webRequest: WebRequest): ResponseEntity<ErrorMessage>{
        val errorMessage = ErrorMessage(
            error = HttpStatus.NOT_FOUND.value(),
            message = ex.message?:"",
            time = LocalDateTime.now())

        return ResponseEntity<ErrorMessage>(errorMessage, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun globalExceptionHandler(ex: Exception,
                                  webRequest: WebRequest): ResponseEntity<ErrorMessage>{
        val errorMessage = ErrorMessage(
            error = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = ex.message?:"",
            time = LocalDateTime.now())

        return ResponseEntity<ErrorMessage>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
