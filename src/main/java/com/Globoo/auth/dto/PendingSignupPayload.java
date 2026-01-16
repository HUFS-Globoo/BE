package com.Globoo.auth.dto;

import com.Globoo.user.domain.Gender;

import java.time.LocalDate;

public record PendingSignupPayload(
        String email,
        String username,
        String encodedPassword,
        String name,
        String phoneNumber,
        String nickname,
        LocalDate birthDate,
        Gender gender
) {}
