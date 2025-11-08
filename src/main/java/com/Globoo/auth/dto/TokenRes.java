package com.Globoo.auth.dto;

public record TokenRes(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSec,
        Long userId //매칭이랑 다른 기능들을 위해 넘기기!

) {}
