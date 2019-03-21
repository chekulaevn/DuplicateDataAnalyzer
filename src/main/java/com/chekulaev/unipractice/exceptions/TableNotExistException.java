package com.chekulaev.unipractice.exceptions;

public class TableNotExistException extends RuntimeException {
    public TableNotExistException() {
        super();
    }

    public TableNotExistException(String message) {
        super(message);
    }

    public TableNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
