package dev.tactiletech.titaniumdriver.database.exceptions.database;

public class FailedToCheckIfCollectionExists extends RuntimeException {
    public FailedToCheckIfCollectionExists(String message) {
        super(message);
    }
}
