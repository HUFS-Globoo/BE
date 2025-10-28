package com.Globoo.common.error.exception;

import com.Globoo.common.error.ErrorCode;

public class BusinessException extends com.Globoo.common.error.exception.BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}