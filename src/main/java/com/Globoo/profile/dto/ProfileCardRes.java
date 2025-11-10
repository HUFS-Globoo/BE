package com.Globoo.profile.dto;

import com.Globoo.user.domain.Campus;
import com.fasterxml.jackson.annotation.JsonProperty;   // 추가

import java.util.List;

public record ProfileCardRes(
        Long userId,
        String nickname,
        Campus campus,
        String country,
        String mbti,
        @JsonProperty("profileImageUrl")  // JSON 키를 profileImageUrl로 for FE
        String profileImage,
        List<LanguageDto> nativeLanguages,
        List<LanguageDto> learnLanguages,
        List<KeywordDto> keywords,
        String infoTitle,
        String infoContent
) {}
