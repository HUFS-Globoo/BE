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

    private Boolean acceptedA;
    private Boolean acceptedB;

    /** ChatRoom PK와 타입 일치 (Long) */
    @Column(name = "chat_room_id") //db 구조확인
    private Long chatRoomId;

    private LocalDateTime matchedAt;
    private String matchedBy;
}
