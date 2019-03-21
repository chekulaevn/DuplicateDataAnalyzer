package com.chekulaev.unipractice.exceptions;

public class ConnectionEstablishmentException extends RuntimeException {
    public ConnectionEstablishmentException() {
        super();
    }

    public ConnectionEstablishmentException(String message) {
        super(message);
    }

    public ConnectionEstablishmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
