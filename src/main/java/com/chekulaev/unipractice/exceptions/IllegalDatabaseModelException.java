package com.chekulaev.unipractice.exceptions;

public class IllegalDatabaseModelException extends RuntimeException {
    public IllegalDatabaseModelException() {
        super();
    }

    public IllegalDatabaseModelException(String message) {
        super(message);
    }

    public IllegalDatabaseModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
