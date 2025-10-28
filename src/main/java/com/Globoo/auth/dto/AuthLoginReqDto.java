package com.Globoo.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthLoginReqDto {
    private String email;
    private String password;
}