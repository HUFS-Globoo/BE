package com.Globoo.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatReadResDto {
    private Long roomId;
    private Long readerId; // 누가 읽었는지
}