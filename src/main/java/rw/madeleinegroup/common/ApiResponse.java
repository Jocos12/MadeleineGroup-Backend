package rw.madeleinegroup.common;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic API response wrapper for all endpoints.
 * All API responses must be encapsulated in ApiResponse&lt;T&gt;.
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> validationError(String message, T errors) {
        return new ApiResponse<>(false, message, errors, LocalDateTime.now());
    }

    public static ApiResponse<Map<String, String>> validationError(Map<String, String> errors) {
        return new ApiResponse<>(false, "Validation failed", errors, LocalDateTime.now());
    }

    public static ApiResponseBuilder builder() {
        return new ApiResponseBuilder();
    }

    public static class ApiResponseBuilder {
        private boolean success;
        private String message;
        private Object data;
        private LocalDateTime timestamp = LocalDateTime.now();

        public ApiResponseBuilder success(boolean success) { this.success = success; return this; }
        public ApiResponseBuilder message(String message) { this.message = message; return this; }
        public ApiResponseBuilder data(Object data) { this.data = data; return this; }
        public ApiResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        @SuppressWarnings("unchecked")
        public <T> ApiResponse<T> build() {
            return new ApiResponse<>(success, message, (T) data, timestamp);
        }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
