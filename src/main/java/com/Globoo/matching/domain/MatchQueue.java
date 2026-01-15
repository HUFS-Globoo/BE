package com.Globoo.matching.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "match_queue")
public class MatchQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime enqueuedAt;

    @Column(length = 10)
    private String mbti; // 유저의 MBTI (예: "ENFP")

    @Column(length = 5)
    private String nativeLanguageCode; // 유저의 모국어 (예: "ko")

    @Column(length = 5)
    private String preferredLanguageCode; // 유저가 배우고 싶은 언어들 (예: "en, jp")

    @Column(length = 5)
    private String nationalityCode; // 국적 정보 (예: "KR")

    @Column(columnDefinition = "TEXT")
    private String interests; // 관심사 키워드들 (CSV 형태 저장, 예: "운동,여행,코딩")

    public MatchQueue(Long userId, boolean active, LocalDateTime enqueuedAt) {
        this.userId = userId;
        this.active = active;
        this.enqueuedAt = enqueuedAt;
    }

    // 정보를 모두 받는 생성자
    public MatchQueue(Long userId, boolean active, LocalDateTime enqueuedAt,
                      String mbti, String nativeLanguageCode, String preferredLanguageCode,
                      String nationalityCode, String interests) {
        this.userId = userId;
        this.active = active;
        this.enqueuedAt = enqueuedAt;
        this.mbti = mbti;
        this.nativeLanguageCode = nativeLanguageCode;
        this.preferredLanguageCode = preferredLanguageCode;
        this.nationalityCode = nationalityCode;
        this.interests = interests;
    }
}
