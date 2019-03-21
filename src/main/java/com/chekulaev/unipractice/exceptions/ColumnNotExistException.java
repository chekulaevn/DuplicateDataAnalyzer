package com.chekulaev.unipractice.exceptions;

public class ColumnNotExistException extends RuntimeException {
    public ColumnNotExistException() {
        super();
    }

    public ColumnNotExistException(String message) {
        super(message);
    }

    public ColumnNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
