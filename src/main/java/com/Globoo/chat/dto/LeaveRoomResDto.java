package com.Globoo.chat.dto;

import lombok.Getter;

@Getter
public class LeaveRoomResDto extends BaseWebSocketMessageDto {
    private Long leaverId;
    private Long roomId;

    public LeaveRoomResDto(Long leaverId, Long roomId) {
        this.setType("LEAVE_NOTICE");
        this.leaverId = leaverId;
        this.roomId = roomId;
    }
}