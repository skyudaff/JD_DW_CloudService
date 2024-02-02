package org.example.cloudservice.handler;

import org.example.cloudservice.dto.ErrorDto;
import org.example.cloudservice.exception.ErrorInputDataException;
import org.example.cloudservice.exception.ErrorUserException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionsHandlerAdvice {
    @ExceptionHandler(ErrorUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto invalidUserHandler(ErrorUserException e) {
        return new ErrorDto(e.getId(), e.getMessage());
    }

    @ExceptionHandler(ErrorInputDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto invalidInputDataHandler(ErrorInputDataException e) {
        return new ErrorDto(e.getId(), e.getMessage());
    }
}
