package com.Globoo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthLoginResDto {
    private String accessToken;
    // (필요시 RefreshToken 등 추가)
}