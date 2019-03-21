package com.chekulaev.unipractice.exceptions;

public class InvalidAnalyzerInputException extends RuntimeException {
    public InvalidAnalyzerInputException() {
        super();
    }

    public InvalidAnalyzerInputException(String message) {
        super(message);
    }

    public InvalidAnalyzerInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
