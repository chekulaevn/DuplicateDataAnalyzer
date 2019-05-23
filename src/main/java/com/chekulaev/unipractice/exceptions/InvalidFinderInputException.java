package com.chekulaev.unipractice.exceptions;

public class InvalidFinderInputException extends RuntimeException {
    public InvalidFinderInputException() {
        super();
    }

    public InvalidFinderInputException(String message) {
        super(message);
    }

    public InvalidFinderInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
