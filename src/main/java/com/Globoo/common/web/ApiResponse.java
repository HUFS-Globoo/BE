package com.Globoo.common.web;

import com.Globoo.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "errorCode", "message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;

    private ApiResponse(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    // --- 성공 시 사용하는 정적 메소드 ---

    /** 성공 응답 (데이터 포함) */
    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(true, data, "Success", null);
    }

    /** 성공 응답 (데이터, 커스텀 메시지 포함) */
    public static <T> ApiResponse<T> onSuccess(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // --- 실패 시 사용하는 정적 메소드 ---

    /** 실패 응답 (ErrorCode 객체 사용) */
    public static <T> ApiResponse<T> onFailure(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getMessage(), errorCode.name());
    }

    /** 실패 응답 (커스텀 에러 코드, 메시지 사용) */
    public static <T> ApiResponse<T> onFailure(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
}
