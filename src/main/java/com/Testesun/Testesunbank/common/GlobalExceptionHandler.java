package com.Testesun.Testesunbank.common;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> api(ApiException e) {
        return error(e.getStatus(), e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        String message = "資料格式錯誤";
        if (!e.getBindingResult().getFieldErrors().isEmpty()) {
            org.springframework.validation.FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
            message = fieldError.getField() + "：" + fieldError.getDefaultMessage();
        }
        return error(HttpStatus.BAD_REQUEST, message);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> malformed(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "資料格式錯誤");
    }
    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<?> duplicate(Exception e) {
        return error(HttpStatus.CONFLICT, "Email 已被使用");
    }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<?> database(DataAccessException e) {
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof SQLException) {
            SQLException sqlException = (SQLException) cause;
            if ("45000".equals(sqlException.getSQLState())) {
                HttpStatus status = HttpStatus.BAD_REQUEST;
                if (sqlException.getErrorCode() == 30001) {
                    status = HttpStatus.NOT_FOUND;
                }
                return error(status, sqlException.getMessage());
            }
        }
        return error(HttpStatus.SERVICE_UNAVAILABLE, "資料服務暫時無法使用，請稍後再試");
    }
    private ResponseEntity<?> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
