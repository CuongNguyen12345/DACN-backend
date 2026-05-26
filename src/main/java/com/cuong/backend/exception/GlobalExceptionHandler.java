package com.cuong.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.cuong.backend.model.response.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<AppException>> handleExceptions(Exception exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error(
                ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
                exception.getClass().getSimpleName() + ": " + exception.getMessage()));
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<AppException>> handleAppExceptions(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.badRequest().body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<AppException>> handlingValidationExceptions(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {

        }

        return ResponseEntity.badRequest().body(ApiResponse.error(errorCode));
    }
}
