// src/main/java/com/Globoo/common/web/GlobalExceptionHandler.java
package com.Globoo.common.web;

import com.Globoo.common.error.BaseException;
import com.Globoo.common.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // 500 에러 로깅
@RestControllerAdvice(annotations = RestController.class)  // REST 컨트롤러에만 적용
public class GlobalExceptionHandler {

    /**
     * 모든 커스텀 예외 처리
     * (BaseException을 상속한 EntityNotFoundException, AuthorizationException 등)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        // warn 레벨로 예외 로그
        log.warn("BaseException occurred: {} (HTTP Status: {})",
                errorCode.getMessage(), errorCode.getStatus().value());

        // ErrorCode의 HTTP 상태코드 + 공통 실패 응답
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode));
    }

    /**
     * 처리하지 못한 나머지 모든 예외 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        // 스택트레이스 포함해서 error 레벨 로그
        log.error("Unhandled exception occurred", ex);

        // 500 + 공통 에러 응답
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
