package dev.tactiletech.titaniumdriver.database.exceptions;

public class ConnectionFailedException extends RuntimeException {
    public ConnectionFailedException(String message) {
        super(message);
    }
}
