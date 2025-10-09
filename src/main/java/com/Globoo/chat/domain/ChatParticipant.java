package com.Globoo.chat.domain;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"room_id","userId"}))
public class ChatParticipant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id")
    private ChatRoom room;

    private Long userId;
    private boolean joined = true;
}
