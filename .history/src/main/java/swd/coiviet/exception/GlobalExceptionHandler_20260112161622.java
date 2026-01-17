package swd.coiviet.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import swd.coiviet.dto.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        ErrorCode ec = ex.getErrorCode();
        ApiResponse<Object> response = (ex.getData() != null)
                ? ApiResponse.error(ec, ex.getData())
                : ApiResponse.error(ec);

        HttpStatus status = ec.getHttpStatus();
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Invalid request format";
        if (ex.getMessage() != null && ex.getMessage().contains("Content-Type")) {
            message = "Unsupported Content-Type. Please use 'application/json'.";
        }
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), message);
        return new ResponseEntity<>(response, ErrorCode.INVALID_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.INVALID_REQUEST, errors);
        return new ResponseEntity<>(response, ErrorCode.INVALID_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "Internal server error");
        return new ResponseEntity<>(response, ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}
