package com.Globoo.common.error;

public class AuthException extends BaseException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
