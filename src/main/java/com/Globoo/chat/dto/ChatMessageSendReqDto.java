package com.Globoo.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageSendReqDto extends BaseWebSocketMessageDto {
    private Long roomId;
    private String message;
}