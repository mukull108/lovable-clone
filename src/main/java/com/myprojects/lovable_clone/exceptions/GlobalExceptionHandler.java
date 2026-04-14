package com.myprojects.lovable_clone.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException ex){
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getResourceName() + " with id " + ex.getResourceId() + " not found");
        log.error("Resource not found: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex){
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        log.error("Bad request: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiFieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Input Validation Failed",fieldErrors);
        log.error("Not Valid request: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}
