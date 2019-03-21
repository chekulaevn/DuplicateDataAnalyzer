package com.chekulaev.unipractice.exceptions;

public class TableIsEmptyException extends RuntimeException {
    public TableIsEmptyException() {
        super();
    }

    public TableIsEmptyException(String message) {
        super(message);
    }

    public TableIsEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
