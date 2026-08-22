package com.smart.home.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.smart.home.controller")
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, resolveMessage(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String message = fieldError.getDefaultMessage();
            if (message == null || message.startsWith("{") && message.endsWith("}")) {
                String code = message == null ? fieldError.getCode() : message.substring(1, message.length() - 1);
                message = messageSource.getMessage(code, fieldError.getArguments(), code, locale);
            }
            errors.put(fieldError.getField(), message);
        }
        return buildResponse(HttpStatus.BAD_REQUEST, message("validation.failed"), errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, resolveMessage(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, resolveMessage(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message("unexpected.server.error"));
    }

    private String message(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }

    private String resolveMessage(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.startsWith("{") && value.endsWith("}")) {
            String code = value.substring(1, value.length() - 1);
            return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
        }
        return value;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return buildResponse(status, message, Map.of());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Map<String, ?> details) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message);
        payload.put("details", details);
        return ResponseEntity.status(status).body(payload);
    }
}
