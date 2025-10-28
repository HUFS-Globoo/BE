package com.Globoo.chat.dto;

import com.Globoo.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageSendResDto {
    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private LocalDateTime sentAt;

    public static ChatMessageSendResDto from(ChatMessage message) {
        return new ChatMessageSendResDto(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}