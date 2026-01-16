package com.Globoo.auth.dto;

public record VerifyRes(
        boolean verified,
        Long userId,
        String onboardingToken
) {}
