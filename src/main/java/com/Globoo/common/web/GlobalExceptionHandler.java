package com.Globoo.common.web;

import com.Globoo.common.error.BaseException;
import com.Globoo.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    /** 모든 커스텀 예외 처리 */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("BaseException occurred: {} (HTTP Status: {})",
                errorCode.getMessage(), errorCode.getStatus().value());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode));
    }

    /** Validation 예외 처리 (@Valid DTO 검증 실패) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        FieldError fe = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);

        String field = (fe == null) ? "unknown" : fe.getField();
        String msg = (fe == null) ? "입력값이 올바르지 않습니다." : fe.getDefaultMessage();

        // 로그에 필드 + 메시지 찍히게
        log.warn("Validation failed: field={}, message={}", field, msg);

        // ApiResponse 구조에 맞춰 "구체 메시지"를 내려줌
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INVALID_REQUEST.name(), msg));
    }

    /** 처리하지 못한 나머지 모든 예외 (500) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled exception occurred", ex);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
