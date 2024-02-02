package org.example.cloudservice.exception;

import lombok.Getter;

@Getter
public class ErrorUserException extends RuntimeException {
    private final long id;

    public ErrorUserException(String msg, long id) {
        super(msg);
        this.id = id;
    }
}
