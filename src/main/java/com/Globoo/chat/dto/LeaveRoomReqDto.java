package com.Globoo.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LeaveRoomReqDto extends BaseWebSocketMessageDto {
    private Long roomId;
}