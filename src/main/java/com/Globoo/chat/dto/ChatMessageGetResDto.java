package com.Globoo.chat.dto;

import com.Globoo.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageGetResDto {
    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private LocalDateTime sentAt;

    public static ChatMessageGetResDto from(ChatMessage message) {
        return new ChatMessageGetResDto(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}