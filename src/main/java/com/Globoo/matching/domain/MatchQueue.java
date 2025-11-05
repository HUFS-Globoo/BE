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

    public MatchQueue(Long userId, boolean active, LocalDateTime enqueuedAt) {
        this.userId = userId;
        this.active = active;
        this.enqueuedAt = enqueuedAt;
    }
}
