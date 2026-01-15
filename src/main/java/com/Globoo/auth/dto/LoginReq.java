package com.Globoo.auth.dto;

import jakarta.validation.constraints.NotBlank;


public record LoginReq(
        @NotBlank String email,
        @NotBlank String username,
        @NotBlank String password
) {}
