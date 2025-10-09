package com.Globoo.chat.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // 랜덤매칭이면 "match-{uuid}" 등
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // getter/setter 생략(롬복 쓰면 @Getter/@Setter)
}
