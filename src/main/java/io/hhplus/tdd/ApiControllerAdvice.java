package io.hhplus.tdd;

import io.hhplus.tdd.point.HandlePointException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(value = HandlePointException.class)
    public ResponseEntity<ErrorResponse> handlePointException(Exception e) {
        return ResponseEntity.status(402).body(new ErrorResponse("402", e.getMessage()));
    }

}
