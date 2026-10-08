package com.example.exception;

import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMessage resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return buildMessage(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage badRequestException(BadRequestException ex, WebRequest request) {
        return buildMessage(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Se dispara cuando falla @Valid sobre el cuerpo de la peticion. Devolvemos
     * todos los errores de los campos en un solo mensaje, por ejemplo:
     * "title: El titulo del tutorial es obligatorio; description: ..."
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage validationException(MethodArgumentNotValidException ex, WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildMessage(HttpStatus.BAD_REQUEST, detalle, request);
    }

    // JSON mal formado o con tipos incorrectos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage unreadableBodyException(HttpMessageNotReadableException ex, WebRequest request) {
        return buildMessage(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no tiene un JSON valido", request);
    }

    /**
     * Manejador general. Como captura Exception, tambien recibiria las excepciones
     * propias de Spring MVC (ruta inexistente = 404, metodo no permitido = 405,
     * etc.) y las convertiria en 500. Todas ellas implementan ErrorResponse, asi que
     * respetamos el estado HTTP que traen y solo usamos 500 para el resto.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        HttpStatusCode status = (ex instanceof ErrorResponse errorResponse)
                ? errorResponse.getStatusCode()
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorMessage message = new ErrorMessage(
                status.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, status);
    }

    private ErrorMessage buildMessage(HttpStatus status, String message, WebRequest request) {
        return new ErrorMessage(
                status.value(),
                new Date(),
                message,
                request.getDescription(false));
    }

}
