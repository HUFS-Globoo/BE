package com.Globoo.chat.dto;

import lombok.Getter; // ⬅️ @AllArgsConstructor 대신 사용

@Getter
public class ReadReceiptResDto extends BaseWebSocketMessageDto {
    private Long readerId;
    private Long roomId;
    private Long lastReadMessageId;

    public ReadReceiptResDto(Long readerId, Long roomId, Long lastReadMessageId) {
        this.readerId = readerId;
        this.roomId = roomId;
        this.lastReadMessageId = lastReadMessageId;
    }
}