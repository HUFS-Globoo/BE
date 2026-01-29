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

import java.util.NoSuchElementException;

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

        log.warn("Validation failed: field={}, message={}", field, msg);

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INVALID_REQUEST.name(), msg));
    }

    /**
     * 매칭/채팅 플로우에서 cleanup 등으로 이미 삭제된 matchId로 접근할 때 자주 발생.
     * 기존에는 500으로 떨어져 프론트가 처리 불가 → 404로 내려 UX 처리 가능하게 함.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElement(NoSuchElementException ex) {
        log.warn("NoSuchElementException occurred: {}", ex.getMessage());
        return ResponseEntity
                .status(404)
                .body(ApiResponse.onFailure("MATCH_NOT_FOUND", "이미 만료되었거나 삭제된 매칭입니다. 다시 매칭해주세요."));
    }

    /** 참가자 아닌 사람이 요청했을 때 등 (accept/skip 호출 방어) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException occurred: {}", ex.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INVALID_REQUEST.name(), ex.getMessage()));
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
