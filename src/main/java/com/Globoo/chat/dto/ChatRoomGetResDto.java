package com.Globoo.chat.dto;

import com.Globoo.chat.domain.ChatRoom;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ChatRoomGetResDto {
    private Long roomId;
    private String otherParticipantNickname; // 1:1 채팅 상대방 닉네임
    private String lastMessage;
    private LocalDateTime lastMessageAt;

    public ChatRoomGetResDto(ChatRoom chatRoom, String otherParticipantNickname) {
        this.roomId = chatRoom.getId();
        this.otherParticipantNickname = otherParticipantNickname;
        this.lastMessage = chatRoom.getLastMessage();
        this.lastMessageAt = chatRoom.getLastMessageAt();
    }
}