package com.Globoo.profile.dto;

import com.Globoo.user.domain.Campus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

/**
 * 유저 프로필 정보를 담는 DTO (Record 버전)
 */
public record ProfileCardRes(
        Long userId,
        String nickname,
        Campus campus,
        String country, // nationalityCode 대용
        String mbti,

        @JsonProperty("profileImageUrl") // JSON 응답 시 키값 통일
        String profileImage,

        List<LanguageDto> nativeLanguages,
        List<LanguageDto> learnLanguages,
        List<KeywordDto> keywords,

        String infoTitle,
        String infoContent
) {
        /**
         *  MatchingService 전용: 모국어 코드 추출 (첫 번째 언어 기준)
         */
        public String nativeLanguageCode() {
                return (nativeLanguages != null && !nativeLanguages.isEmpty())
                        ? nativeLanguages.get(0).code() : null; // LanguageDto가 record이므로 code() 사용
        }

        /**
         *  MatchingService 전용: 학습 희망 언어 코드 추출 (첫 번째 언어 기준)
         */
        public String preferredLanguageCode() {
                return (learnLanguages != null && !learnLanguages.isEmpty())
                        ? learnLanguages.get(0).code() : null; // LanguageDto가 record이므로 code() 사용
        }

        /**
         *  MatchingService 전용: 국가 코드를 nationalityCode 명칭으로 반환
         */
        public String nationalityCode() {
                return country;
        }

        /**
         *  MatchingService 전용: 카테고리별 키워드 필터링 접근자들
         */
        public List<String> personalityKeywords() { return filterKeywordsByCategory("PERSONALITY"); }
        public List<String> hobbyKeywords() { return filterKeywordsByCategory("HOBBY"); }
        public List<String> topicKeywords() { return filterKeywordsByCategory("TOPIC"); }

        /**
         *  타입 불일치 및 메서드명 에러를 해결한 핵심 필터링 로직
         */
        private List<String> filterKeywordsByCategory(String category) {
                if (keywords == null) return List.of();
                return keywords.stream()
                        .filter(k -> category.equals(k.category())) // KeywordDto가 record이므로 category() 호출
                        .<String>map(k -> k.name()) // record는 name() 호출 및 <String> 타입 명시로 List<Object> 에러 방지
                        .toList();
        }

        /**
         *  MatchingService 전용: 모든 관심사 키워드를 하나의 리스트로 통합
         */
        public List<String> getInterests() {
                List<String> combined = new ArrayList<>();
                combined.addAll(personalityKeywords());
                combined.addAll(hobbyKeywords());
                combined.addAll(topicKeywords());
                return combined;
        }
}