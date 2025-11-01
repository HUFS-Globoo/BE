package com.Globoo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshReq(@NotBlank String refreshToken) {}
