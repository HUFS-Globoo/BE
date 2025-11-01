package com.Globoo.profile.dto;

import com.Globoo.user.domain.Campus;
import com.Globoo.user.domain.Gender;

import java.util.List;

public record ProfileDetailRes(
        Long userId,
        String email,
        String nickname,
        String name,                 // /api/me일 때만 사용
        Campus campus,
        String country,
        String mbti,
        String profileImage,         // Profile.profileImage
        String infoTitle,
        String infoContent,
        String birthDate,            // YYYY-MM-DD (Profile.birthDate)
        Gender gender,               // Profile.gender
        List<LanguageDto> nativeLanguages,
        List<LanguageDto> learnLanguages,
        List<KeywordDto> keywords
) {}
