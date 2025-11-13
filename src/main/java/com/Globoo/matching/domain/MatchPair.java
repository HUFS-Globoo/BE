// src/main/java/com/Globoo/matching/domain/MatchPair.java
package com.Globoo.matching.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "match_pair")
public class MatchPair {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Long userAId;

    @Column(nullable = false)
    private Long userBId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    /**
     * Boolean 기본값을 null → false 로 통일
     * Builder 사용 시에도 false가 유지되도록 @Builder.Default 적용
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean acceptedA = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean acceptedB = false;

    /** ChatRoom PK와 타입 일치 (Long) */
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    private LocalDateTime matchedAt;
    private String matchedBy;
}
