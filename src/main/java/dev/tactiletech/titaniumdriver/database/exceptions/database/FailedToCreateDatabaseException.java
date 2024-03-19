package dev.tactiletech.titaniumdriver.database.exceptions.database;

public class FailedToCreateDatabaseException extends RuntimeException {
    public FailedToCreateDatabaseException(String message) {
        super(message);
    }
}
