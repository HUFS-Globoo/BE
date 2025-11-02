package com.Globoo.auth.dto;

public record TokenRes(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSec
) {}
