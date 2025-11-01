// src/main/java/com/Globoo/auth/dto/SignupReq.java
package com.Globoo.auth.dto;

import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.Gender;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record SignupReq(
        // 기본 회원정보
        @Email @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@hufs\\.ac\\.kr$", message = "@hufs.ac.kr 이메일만 가능합니다.")
        String email,

        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(max = 50) String name,

        @Pattern(regexp = "^[0-9]{8,15}$") String phoneNumber,   // optional 허용
        @NotBlank @Size(min = 2, max = 30) String nickname,
        LocalDate birthDate,             // optional
        Gender gender,                   // optional (MALE/FEMALE)
        Campus campus,                   // optional (SEOUL/GLOBAL)

        // 온보딩 (회원가입 단계에서 함께 받기)
        @NotBlank String nativeLanguageCode,     // ex) "ko"  (languages.code)
        @NotBlank String preferredLanguageCode,  // ex) "en"  (languages.code)
        @NotBlank String nationalityCode,        // profiles.country에 저장 (문자열 자유)

        @NotBlank String mbti,                   // ex) ENFP (DDL은 VARCHAR)

        @Size(max = 10) List<@NotBlank String> personalityKeywords,
        @Size(max = 10) List<@NotBlank String> hobbyKeywords,
        @Size(max = 10) List<@NotBlank String> topicKeywords
) {}
