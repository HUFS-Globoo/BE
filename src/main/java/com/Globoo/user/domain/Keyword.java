package com.Globoo.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keywords")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    // DDL 반영: 카테고리 (PERSONALITY / HOBBY / TOPIC)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Category category;

    @Builder.Default
    @Column(nullable = false)
    private int sortOrder = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public enum Category {
        PERSONALITY, HOBBY, TOPIC
    }
}
