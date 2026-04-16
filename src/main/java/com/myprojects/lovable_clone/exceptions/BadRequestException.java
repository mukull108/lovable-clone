package com.myprojects.lovable_clone.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
public class BadRequestException extends RuntimeException{
    String message;
}
