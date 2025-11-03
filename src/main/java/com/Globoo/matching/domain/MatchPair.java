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
    private UUID id; // 매칭 ID

    @Column(nullable = false)
    private Long userAId;

    @Column(nullable = false)
    private Long userBId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private Boolean acceptedA;
    private Boolean acceptedB;

    // [수정] 채팅방 ID 타입을 Long 으로 변경 (DB 컬럼도 변경됨)
    private Long chatRoomId;
    private LocalDateTime matchedAt;
    private String matchedBy;
}