package com.Globoo.common.error;

public class EntityNotFoundException extends com.Globoo.common.error.BaseException {
    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    // 기본 에러 코드를 사용할 경우
    public EntityNotFoundException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}