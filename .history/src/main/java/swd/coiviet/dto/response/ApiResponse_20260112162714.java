package swd.coiviet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import swd.coiviet.exception.ErrorCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;
    private Object errors;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Legacy-compatible constructor (code, message, data)
    public ApiResponse(int code, String message, T data) {
        this.success = code >= 200 && code < 300;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Success factories
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.code = 200;
        r.message = "Thành công";
        r.data = data;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.code = 200;
        r.message = message;
        r.data = data;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    // Error factories
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = code;
        r.message = message;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static <T> ApiResponse<T> error(int code, String message, Object errors) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = code;
        r.message = message;
        r.errors = errors;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Object data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = errorCode.getCode();
        r.message = errorCode.getMessage();
        //noinspection unchecked
        r.data = (T) data;
        r.timestamp = LocalDateTime.now();
        return r;
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Object data, Object errors) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.code = errorCode.getCode();
        r.message = errorCode.getMessage();
        //noinspection unchecked
        r.data = (T) data;
        r.errors = errors;
        r.timestamp = LocalDateTime.now();
        return r;
    }

}
