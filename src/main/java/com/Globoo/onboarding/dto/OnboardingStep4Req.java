package com.Globoo.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OnboardingStep4Req(
        @NotBlank String mbti,
        @Size(max = 10) List<@NotBlank String> personalityKeywords,
        @Size(max = 10) List<@NotBlank String> hobbyKeywords,
        @Size(max = 10) List<@NotBlank String> topicKeywords
) {}
