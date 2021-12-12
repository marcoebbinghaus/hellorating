package de.codinghaus.hellorating.exception

import de.codinghaus.hellorating.exception.model.EntityNotFoundException
import de.codinghaus.hellorating.exception.model.ErrorType
import de.codinghaus.hellorating.exception.model.HttpError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandlers : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [(EntityNotFoundException::class)])
    fun handleEntityNotFound(ex: EntityNotFoundException, request: WebRequest): ResponseEntity<HttpError> {
        return ResponseEntity(HttpError(ErrorType.ERROR, ex.message), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(value = [(IllegalArgumentException::class)])
    fun handleIllegalArgument(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<HttpError> {
        return ResponseEntity(HttpError(ErrorType.ERROR, ex.message ?: ""), HttpStatus.BAD_REQUEST)
    }
}