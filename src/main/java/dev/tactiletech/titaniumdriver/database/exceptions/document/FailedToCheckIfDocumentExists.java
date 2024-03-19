package dev.tactiletech.titaniumdriver.database.exceptions.document;

public class FailedToCheckIfDocumentExists extends RuntimeException {
    public FailedToCheckIfDocumentExists(String message) {
        super(message);
    }
}
