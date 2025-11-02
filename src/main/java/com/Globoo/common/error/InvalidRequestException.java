package com.Globoo.common.error;

public class InvalidRequestException extends com.Globoo.common.error.BaseException {
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}