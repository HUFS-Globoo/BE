package com.Globoo.common.error;

public class AuthorizationException extends BaseException {
    public AuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }
}