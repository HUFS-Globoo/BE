package com.Globoo.chat.dto;

import lombok.Getter;

@Getter
public class ChatRoomCreateResDto {

    private Long roomId;
    public ChatRoomCreateResDto(Long roomId) {
        this.roomId = roomId;
    }
}