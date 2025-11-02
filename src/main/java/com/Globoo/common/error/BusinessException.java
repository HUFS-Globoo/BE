package com.Globoo.common.error;

public class BusinessException extends com.Globoo.common.error.BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}