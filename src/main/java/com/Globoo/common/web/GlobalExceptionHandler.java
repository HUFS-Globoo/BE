package com.Globoo.common.web;

import com.Globoo.common.error.ErrorCode;
import com.Globoo.common.error.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // 500 에러 로깅
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**  모든 커스텀 예외 처리 (BaseException을 상속한 EntityNotFoundException, AuthorizationException 등 모두 처리) */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        // 1. 발생한 예외로부터 ErrorCode를 가져옵니다.
        ErrorCode errorCode = ex.getErrorCode();

        // 2. 예외 로그 남기기 (warn 레벨)
        log.warn("BaseException occurred: {} (HTTP Status: {})",
                errorCode.getMessage(), errorCode.getStatus().value());

        // 3. ApiResponse.onFailure()를 사용해 실패 응답을 생성합니다.
        // 4. ResponseEntity.status()로 ErrorCode의 HTTP 상태 코드를 설정합니다.
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode));
    }

    /** 처리하지 못한 나머지 모든 예외 처리 (500 Internal Server Error) (반드시 필요) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        // 1. 처리되지 않은 예외는 심각한 문제일 수 있으므로 stack trace를 포함하여 error 레벨로 로깅
        log.error("Unhandled exception occurred", ex);

        // 2. 클라이언트에게는 500 에러와 공통 메시지를 반환
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}