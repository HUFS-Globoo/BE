package com.Globoo.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public record OnboardingStep3Req(
        @NotBlank String nationalityCode,
        @NotBlank String nativeLanguageCode,
        @NotBlank String preferredLanguageCode
) {}
