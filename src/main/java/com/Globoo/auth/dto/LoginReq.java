package com.Globoo.auth.dto;

import jakarta.validation.constraints.NotBlank;


public record LoginReq(
        String email,
        String username,
        @NotBlank String password
) {}
