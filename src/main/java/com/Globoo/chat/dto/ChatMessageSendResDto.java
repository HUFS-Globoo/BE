package com.Globoo.chat.dto;

import com.Globoo.chat.domain.ChatMessage;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageSendResDto extends BaseWebSocketMessageDto {
    private Long messageId;
    private Long senderId;
    private String senderNickname;
    private String senderProfileImageUrl;
    private String message;
    private LocalDateTime sentAt;

    private ChatMessageSendResDto(Long messageId, Long senderId, String senderNickname, String senderProfileImageUrl, String message, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.senderProfileImageUrl = senderProfileImageUrl;
        this.message = message;
        this.sentAt = sentAt;
    }

    public static ChatMessageSendResDto from(ChatMessage message) {
        return new ChatMessageSendResDto(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getUsername(),
                message.getSender().getProfileImageUrl(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}