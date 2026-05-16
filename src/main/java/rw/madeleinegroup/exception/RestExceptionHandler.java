package rw.madeleinegroup.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rw.madeleinegroup.common.ApiResponse;

/**
 * Maps deserialization and argument errors to HTTP 400 with a clear message (e.g. invalid {@code audience}).
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String message = ex.getMessage();
        if (cause instanceof IllegalArgumentException ia) {
            message = ia.getMessage();
        } else if (cause instanceof InvalidFormatException ife) {
            message = ife.getOriginalMessage() != null ? ife.getOriginalMessage() : ife.getMessage();
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                message = "Invalid value for enum field. Use one of the allowed constant names (e.g. ALL_TEAM).";
            }
        }
        if (message == null || message.isBlank()) {
            message = "Invalid request body";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }
}
