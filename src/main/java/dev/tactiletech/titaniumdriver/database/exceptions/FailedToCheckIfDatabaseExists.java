package dev.tactiletech.titaniumdriver.database.exceptions;

public class FailedToCheckIfDatabaseExists extends RuntimeException {
    public FailedToCheckIfDatabaseExists(String message) {
        super(message);
    }
}
