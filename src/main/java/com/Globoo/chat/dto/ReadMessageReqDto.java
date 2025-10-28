package com.Globoo.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReadMessageReqDto extends BaseWebSocketMessageDto {
    private Long roomId;
    private Long lastReadMessageId;
}