// src/main/java/com/Globoo/user/dto/MyPageRes.java
package com.Globoo.user.dto;

import com.Globoo.user.domain.Campus;
import lombok.*;

import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class MyPageRes {
    // 상단 카드
    private String name;            // 실명
    private String nickname;        // 닉네임
    private String mbti;
    private String profileImageUrl; // null이면 프론트에서 기본 썸네일 사용

    // 소개 영역
    private String infoTitle;
    private String infoContent;

    // 기본 정보
    private Campus campus;          // SEOUL / GLOBAL
    private String country;         // ex) "KR"
    private String email;

    // 언어
    private List<String> nativeLanguages; // ["ko", "ja"]
    private List<String> learnLanguages;  // ["en"]

    // 키워드
    private List<String> personalityKeywords;
    private List<String> hobbyKeywords;
    private List<String> topicKeywords;
}
