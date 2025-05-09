package tech.thepack.core.resource.exception;

import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ErrorResponse(HttpStatus httpStatus, String message, String path) {
        this(LocalDateTime.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message, path, null);
    }

    public ErrorResponse(HttpStatus httpStatus, String message, String path, List<String> details) {
        this(LocalDateTime.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message, path, details);
    }

}