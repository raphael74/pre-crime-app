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
    fun handleAll(
        ex: Exception,
        request: HttpServletRequest?,
    ): ResponseEntity<Any> {
        // Full details are logged server-side only; a generic message is returned to the
        // client to avoid disclosing internal implementation details (e.g. stack traces,
        // SQL errors, class names) which security scanners such as OWASP ZAP flag as
        // information disclosure.
        logger.warn("Exception in call ${request?.requestURI}:", ex)

        return ResponseEntity(GENERIC_ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    companion object {
        private const val GENERIC_ERROR_MESSAGE = "An unexpected error occurred. Please contact the administrator."
    }
}
