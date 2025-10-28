package com.Globoo.common.error.exception;

import com.Globoo.common.error.ErrorCode;

public class AuthorizationException extends BaseException {
    public AuthorizationException(ErrorCode errorCode) {
        super(errorCode);
    }
}