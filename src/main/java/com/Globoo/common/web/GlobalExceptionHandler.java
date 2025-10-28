package com.Globoo.common.web;


import com.Globoo.common.error.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handle(BusinessException ex){
        return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getMessage()));
    }
}
