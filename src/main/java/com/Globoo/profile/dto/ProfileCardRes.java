package com.Globoo.profile.dto;

import com.Globoo.user.domain.Campus;

import java.util.List;

public record ProfileCardRes(
        Long userId,
        String nickname,
        Campus campus,
        String country,
        String mbti,
        String profileImage,
        List<LanguageDto> nativeLanguages,
        List<LanguageDto> learnLanguages,
        List<KeywordDto> keywords,
        String infoTitle,
        String infoContent
) {}
