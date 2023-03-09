package com.itmo.microservices.demo.common.exceptions.handlers
import com.itmo.microservices.demo.common.exceptions.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.ZoneId
import java.time.ZonedDateTime

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(value = [AccessDeniedException::class])
    fun handleApiRequestException(e: AccessDeniedException): ResponseEntity<ApiException> {
        return ResponseEntity(
            ApiException(e.message, HttpStatus.FORBIDDEN, ZonedDateTime.now(ZoneId.of("Z"))),
            HttpStatus.FORBIDDEN
        )
    }
    @ExceptionHandler(value = [EntityViolationException::class])
    fun handleApiRequestException(e: EntityViolationException): ResponseEntity<ApiException> {
        return ResponseEntity(
            ApiException(e.message, HttpStatus.CONFLICT, ZonedDateTime.now(ZoneId.of("Z"))),
            HttpStatus.CONFLICT
        )
    }

    @ExceptionHandler(value = [BadRequestException::class])
    fun handleApiRequestException(e: BadRequestException): ResponseEntity<ApiException> {
        return ResponseEntity(
            ApiException(e.message, HttpStatus.BAD_REQUEST, ZonedDateTime.now(ZoneId.of("Z"))),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(value = [NotFoundException::class])
    fun handleApiRequestException(e: NotFoundException): ResponseEntity<ApiException> {
        return ResponseEntity(
            ApiException(e.message, HttpStatus.NOT_FOUND, ZonedDateTime.now(ZoneId.of("Z"))),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(value = [ExternalServiceException::class])
    fun handleApiRequestException(e: ExternalServiceException): ResponseEntity<ApiException> {
        return ResponseEntity(
            ApiException(e.message, HttpStatus.FAILED_DEPENDENCY, ZonedDateTime.now(ZoneId.of("Z"))),
            HttpStatus.FAILED_DEPENDENCY
        )
    }
}