package dev.tactiletech.titaniumdriver.database.exceptions.document;

public class FailedToDeleteDocumentException extends RuntimeException {
    public FailedToDeleteDocumentException(String message) {
        super(message);
    }
}
