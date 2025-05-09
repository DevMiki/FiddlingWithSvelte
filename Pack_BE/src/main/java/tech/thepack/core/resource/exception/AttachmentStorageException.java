package tech.thepack.core.resource.exception;

public class AttachmentStorageException extends RuntimeException {

    public AttachmentStorageException(String message) {
        super(message);
    }

    public AttachmentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}