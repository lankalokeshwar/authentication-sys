package com.example.Authentication_sys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(AppExceptions.UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) //404
    public Map<String, Object> handleUserNotFound(AppExceptions.UserNotFoundException ex) {
        return Map.of("success", false, "message", ex.getMessage());
    }

    @ExceptionHandler(AppExceptions.InvalidOtpException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInvalidOtp(AppExceptions.InvalidOtpException ex) {
        return Map.of("success", false, "message", ex.getMessage());
    }

    @ExceptionHandler(AppExceptions.UserBlockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleUserBlocked(AppExceptions.UserBlockedException ex) {
        return Map.of("success", false, "message", ex.getMessage());
    }

    @ExceptionHandler(AppExceptions.EmailNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleEmailNotVerified(AppExceptions.EmailNotVerifiedException ex) {
        return Map.of("success", false, "message", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneral(Exception ex) {
        return Map.of("success", false, "message", "An unexpected error occurred: " + ex.getMessage());
    }
}
