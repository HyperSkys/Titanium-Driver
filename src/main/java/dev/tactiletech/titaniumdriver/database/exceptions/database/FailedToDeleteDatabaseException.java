package dev.tactiletech.titaniumdriver.database.exceptions.database;

public class FailedToDeleteDatabaseException extends RuntimeException {
    public FailedToDeleteDatabaseException(String message) {
        super(message);
    }
}
