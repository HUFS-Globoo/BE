package com.Globoo.auth.dto;

public record SignupRes(
        Long id,
        String email,
        String username,
        String nickname,
        boolean schoolVerified
) {}
