package com.Globoo.profile.dto;

import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;   // 추가

import java.util.List;

public record ProfileDetailRes(
        Long userId,
        String email,
        String nickname,
        String name,                 // /api/me일 때만 사용
        Campus campus,
        String country,
        String mbti,
        @JsonProperty("profileImageUrl")  // JSON 키만 profileImageUrl로 변경 for FE
        String profileImage,         // Profile.profileImage
        String infoTitle,
        String infoContent,
        String birthDate,            // YYYY-MM-DD (Profile.birthDate)
        Gender gender,               // Profile.gender
        List<LanguageDto> nativeLanguages,
        List<LanguageDto> learnLanguages,
        List<KeywordDto> keywords
) {}
