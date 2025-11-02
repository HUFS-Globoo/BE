package com.Globoo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendReq(@Email @NotBlank String email) {}
