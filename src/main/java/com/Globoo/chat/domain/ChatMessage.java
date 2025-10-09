package com.Globoo.chat.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id")
    private ChatRoom room;

    private Long senderId;        // User ID
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sentAt = LocalDateTime.now();
}
