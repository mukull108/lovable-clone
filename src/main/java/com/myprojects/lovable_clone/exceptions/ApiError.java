package com.myprojects.lovable_clone.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiError(
        HttpStatus status,
        String message,
        Instant timestamp,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<ApiFieldError> errors
//        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errors

) {
    //constructor with all the filed would be here

    //this is another constructor in which we will provide status and message only
    public ApiError(HttpStatus status, String message) {
        this(status, message, Instant.now(),null);
    }

    public ApiError(HttpStatus status, String message,List<ApiFieldError> errors) {
        this(status, message, Instant.now(),errors);
    }

//     public ApiError(HttpStatus status, String message, Map<String, String> errors) {
//        this(status, message, Instant.now(),errors);
//    }

}

record ApiFieldError(
        String field,
        String message
){

}