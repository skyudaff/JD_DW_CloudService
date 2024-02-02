package org.example.cloudservice.exception;

import lombok.Getter;

@Getter
public class ErrorInputDataException extends RuntimeException {
    private final long id;

    public ErrorInputDataException(String msg, long id) {
        super(msg);
        this.id = id;
    }
}
