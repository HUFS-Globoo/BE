package com.Globoo.common.error.exception;

import com.Globoo.common.error.ErrorCode;

public class InvalidRequestException extends com.Globoo.common.error.exception.BaseException {
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}