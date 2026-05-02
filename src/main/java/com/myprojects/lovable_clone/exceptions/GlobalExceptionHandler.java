package com.myprojects.lovable_clone.exceptions;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

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

//        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
//                .collect(Collectors.toMap(
//                        fieldError -> fieldError.getField(),
//                        fieldError -> fieldError.getDefaultMessage()
//                ));

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Input Validation Failed",fieldErrors);
        log.error("Not Valid request: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFoundExceptions(AuthenticationException ex){
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Username not found with username " + ex.getMessage());
        log.error(apiError.toString(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationExceptions(AuthenticationException exception){
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED,"Authentication failed: "+exception.getMessage());
        log.error(apiError.toString(), exception);
        return new ResponseEntity<>(apiError,HttpStatus.UNAUTHORIZED);
    }

    //this will not work directly from here,for this we will have to pass exception from filter chain context to servlet,
    // global exception handler will only handle exceptions on servlet context
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleJwtException(JwtException exception){
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED,"Invalid JWT token: "+exception.getMessage());
        log.error(apiError.toString(), exception);
        return new ResponseEntity<>(apiError,HttpStatus.UNAUTHORIZED);
    }

    //when we do not have access, if you are not using this then your request will take you to the login page, after
    //adding this you now get forbidden error because you do not have access.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException exception){
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN,"Access denied: Insufficient Access: "+exception.getMessage());
        log.error(apiError.toString(), exception);
        return new ResponseEntity<>(apiError,HttpStatus.FORBIDDEN);
    }
}
