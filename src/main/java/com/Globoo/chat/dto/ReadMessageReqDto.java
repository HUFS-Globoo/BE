package com.Globoo.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReadMessageReqDto extends BaseWebSocketMessageDto {
    private Long roomId;
    private Long lastReadMessageId;
}