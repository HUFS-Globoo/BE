package com.Globoo.chat.domain;

import com.Globoo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatParticipant> participants = new ArrayList<>();

    // 채팅방 목록 조회를 위한 마지막 메시지 정보
    private String lastMessage;
    private LocalDateTime lastMessageAt;

    public void addParticipant(ChatParticipant participant) {
        this.participants.add(participant);
    }

    // 마지막 메시지 업데이트
    public void updateLastMessage(String message, LocalDateTime sentAt) {
        this.lastMessage = message;
        this.lastMessageAt = sentAt;
    }
}