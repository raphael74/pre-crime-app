package org.example.precrime.infrastructure.facade.rest

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class RestExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler
    fun handleAll(ex: Exception, request: HttpServletRequest?): ResponseEntity<Any> {
        logger.warn("Exception in call ${request?.requestURI}:", ex)

        var message = ex.message
        if (ex.cause != null) {
            message += " - "
            message += ex.cause!!.message
        }

        return ResponseEntity(message, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
