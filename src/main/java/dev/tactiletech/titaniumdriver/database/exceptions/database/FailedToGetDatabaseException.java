package dev.tactiletech.titaniumdriver.database.exceptions.database;

public class FailedToGetDatabaseException extends RuntimeException {
    public FailedToGetDatabaseException(String message) {
        super(message);
    }
}
